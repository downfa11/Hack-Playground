package com.ns.solve.service.contest;

import com.ns.solve.domain.dto.contest.*;
import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.contest.Prize;
import com.ns.solve.domain.entity.contest.Team;
import com.ns.solve.domain.entity.user.Affiliation;
import com.ns.solve.domain.entity.user.Role;
import com.ns.solve.domain.entity.user.User;
import com.ns.solve.domain.vo.AffiliationType;
import com.ns.solve.domain.vo.ContestStatus;
import com.ns.solve.domain.vo.ContestType;
import com.ns.solve.domain.vo.WargameKind;
import com.ns.solve.repository.AffiliationRepository;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.repository.contest.ContestRepository;
import com.ns.solve.repository.contest.PrizeRepository;
import com.ns.solve.repository.contest.TeamRepository;
import com.ns.solve.utils.exception.ErrorCode.ContestErrorCode;
import com.ns.solve.utils.exception.SolvedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContestService {
    private final TeamService teamService;

    private final ContestRepository contestRepository;
    private final UserRepository userRepository;
    private final AffiliationRepository affiliationRepository;
    private final PrizeRepository prizeRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public ContestDto createContest(RegisterContestRequest registerContestRequest) {
        if (registerContestRequest.getAffiliations() != null && !registerContestRequest.getAffiliations().isEmpty() &&
                (registerContestRequest.getAffiliationTypes() == null || registerContestRequest.getAffiliationTypes().isEmpty())) {

            throw new IllegalArgumentException("특정 소속을 지정하려면 소속 유형도 함께 선택해야 합니다.");
        }

        validateContestTime(registerContestRequest.getStartTime(), registerContestRequest.getEndTime());


        boolean exists = contestRepository.existsByTitleAndStartTime(registerContestRequest.getTitle(), registerContestRequest.getStartTime());
        if (exists) {
            throw new SolvedException(ContestErrorCode.CONTEST_ALREADY_EXISTS);
        }

        Set<User> organizers = userRepository.findAllById(registerContestRequest.getOrganizerIds()).stream()
                .collect(Collectors.toSet());

        List<Long> affiliationIds = registerContestRequest.getAffiliations() != null
                ? registerContestRequest.getAffiliations().stream()
                .map(a -> a.getId())
                .filter(Objects::nonNull)
                .toList()
                : Collections.emptyList();
        Set<Affiliation> affiliations = affiliationRepository.findAllById(affiliationIds).stream()
                .collect(Collectors.toSet());

        Set<AffiliationType> affiliationTypes = new HashSet<>(registerContestRequest.getAffiliationTypes());

        List<WargameKind> problemKinds = registerContestRequest.getProblemKinds() != null
                ? registerContestRequest.getProblemKinds()
                : Collections.emptyList();
        Set<WargameKind> registeredProblmKinds = new HashSet<>(problemKinds);

        Contest contest = Contest.builder()
                .title(registerContestRequest.getTitle())
                .description(registerContestRequest.getDescription())
                .startTime(registerContestRequest.getStartTime())
                .endTime(registerContestRequest.getEndTime())
                .type(registerContestRequest.getType())
                .problemKinds(registeredProblmKinds)
                .maxTeamSize(registerContestRequest.getMaxTeamSize())
                .organizerName(registerContestRequest.getOrganizerName())
                .organizers(organizers)
                .affiliations(affiliations)
                .affiliationTypes(affiliationTypes)
                .prize(registerContestRequest.isPrizeEnabled() ? registerContestRequest.getPrizeMoney() : null)
                .rules(registerContestRequest.getRules())
                .reviewConsent(registerContestRequest.isReviewConsent())
                .build();

        Contest newContest = contestRepository.save(contest);

        if (registerContestRequest.getPrizes() != null && registerContestRequest.isPrizeEnabled()) {
            List<Prize> prizes = registerContestRequest.getPrizes().stream()
                    .map(dto -> Prize.builder()
                            .rank(dto.getRank())
                            .name(dto.getName())
                            .numberOfWinners(dto.getNumberOfWinners())
                            .contest(newContest)
                            .build())
                    .collect(Collectors.toList());
            prizeRepository.saveAll(prizes);
            newContest.setPrizes(prizes);
        }

        return ContestDto.from(newContest, 0);
    }

    @Transactional(readOnly = true)
    public ContestDto getContestById(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND));

        int teamCount = 0;
        if (contest.getType() != ContestType.INDIVIDUAL) {
            teamCount = teamRepository.countByContestId(contestId);
        }

        return ContestDto.from(contest, teamCount);
    }

    @Transactional(readOnly = true)
    public List<ContestDto> getContests(ContestStatus status, String searchTerm) {
        List<Contest> contests;
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        if (searchTerm != null && !searchTerm.isBlank()) {
            contests = contestRepository.findByTitleContainingIgnoreCase(searchTerm);
        } else if (status != null) {
            switch (status) {
                case UPCOMING:
                    contests = contestRepository.findByStartTimeAfterOrderByStartTimeAsc(now.toLocalDateTime());
                    break;
                case ONGOING:
                    contests = contestRepository.findByStartTimeBeforeAndEndTimeAfterOrderByEndTimeAsc(now.toLocalDateTime(), now.toLocalDateTime());
                    break;
                case ENDED:
                    contests = contestRepository.findByEndTimeBeforeOrderByEndTimeDesc(now.toLocalDateTime());
                    break;
                default:
                    contests = contestRepository.findAll();
            }
        } else {
            contests = contestRepository.findAll();
        }

        return contests.stream()
                .map(contest -> {
                    int teamCount = contest.getType() == ContestType.INDIVIDUAL ? 0 : teamRepository.countByContestId(contest.getId());
                    return ContestDto.from(contest, teamCount);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ContestDto updateContest(Long contestId, ModifyContestRequest modifyContestRequest) {
        if (modifyContestRequest.getAffiliations() != null && !modifyContestRequest.getAffiliations().isEmpty() &&
                (modifyContestRequest.getAffiliationTypes() == null || modifyContestRequest.getAffiliationTypes().isEmpty())) {
            throw new IllegalArgumentException("특정 소속을 지정하려면 소속 유형도 함께 선택해야 합니다.");
        }

        validateContestTime(modifyContestRequest.getStartTime(), modifyContestRequest.getEndTime());

        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND));

        contest.getPrizes().clear();

        if (modifyContestRequest.isPrizeEnabled() && modifyContestRequest.getPrizes() != null) {
            List<Prize> newPrizes = modifyContestRequest.getPrizes().stream()
                    .map(dto -> Prize.builder()
                            .rank(dto.getRank())
                            .name(dto.getName())
                            .numberOfWinners(dto.getNumberOfWinners())
                            .build())
                    .collect(Collectors.toList());

            newPrizes.forEach(prize -> {
                prize.setContest(contest);
                contest.getPrizes().add(prize);
            });
        }

        contest.setTitle(modifyContestRequest.getTitle());
        contest.setDescription(modifyContestRequest.getDescription());
        contest.setStartTime(modifyContestRequest.getStartTime());
        contest.setEndTime(modifyContestRequest.getEndTime());
        contest.setType(modifyContestRequest.getType());
        contest.setProblemKinds(new HashSet<>(modifyContestRequest.getProblemKinds()));
        contest.setMaxTeamSize(modifyContestRequest.getMaxTeamSize());
        contest.setOrganizerName(modifyContestRequest.getOrganizerName());
        contest.setPrize(modifyContestRequest.isPrizeEnabled() ? modifyContestRequest.getPrizeMoney() : null);
        contest.setRules(modifyContestRequest.getRules());

        Set<User> updatedOrganizers = userRepository.findAllById(modifyContestRequest.getOrganizerIds()).stream().collect(Collectors.toSet());
        contest.setOrganizers(updatedOrganizers);

        Set<AffiliationType> affiliationTypes = new HashSet<>(modifyContestRequest.getAffiliationTypes());
        contest.setAffiliationTypes(affiliationTypes);

        List<Long> affiliationIds = modifyContestRequest.getAffiliations() != null
                ? modifyContestRequest.getAffiliations().stream()
                .map(a -> a.getId())
                .filter(Objects::nonNull)
                .toList()
                : Collections.emptyList();
        Set<Affiliation> affiliations = affiliationRepository.findAllById(affiliationIds).stream()
                .collect(Collectors.toSet());

        contest.setAffiliations(affiliations);

        Contest updatedContest = contestRepository.save(contest);

        int teamCount = 0;
        if (contest.getType() != ContestType.INDIVIDUAL) {
            teamCount = teamRepository.countByContestId(contestId);
        }
        return ContestDto.from(updatedContest, teamCount);
    }

    @Transactional
    public ContestResultDto getContestResults(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND));

        // 대회 종료 여부 확인 or 상태 갱신
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        ZonedDateTime contestEndKST = contest.getEndTime().atZone(ZoneId.of("Asia/Seoul"));

        if (contestEndKST.isAfter(now)) {
            throw new SolvedException(ContestErrorCode.CONTEST_NOT_ENDED);
        }


        // 상별 우승자 결정 or 조회
        for (Prize prize : contest.getPrizes()) {
            if (prize.getWinners() == null || prize.getWinners().isEmpty()) {
                Set<User> winners = determineWinnersForPrize(contest, prize);
                prize.setWinners(winners);
            }
        }

        return ContestResultDto.builder()
                .id(contest.getId())
                .title(contest.getTitle())
                .prizes(contest.getPrizes().stream()
                        .map(prize -> ContestResultDto.PrizeResultDto.builder()
                                .rank(prize.getRank())
                                .name(prize.getName())
                                .winners(prize.getWinners().stream()
                                        .map(u -> {
                                            if (contest.getType() == ContestType.INDIVIDUAL) {
                                                return u.getNickname();
                                            } else { // TEAM or GROUP
                                                Team team = teamRepository.findByContestIdAndMembersContains(contest.getId(), u)
                                                        .orElseThrow();
                                                return team.getName() + " (" + u.getNickname() + ")";
                                            }
                                        })
                                        .collect(Collectors.toList()))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private Set<User> determineWinnersForPrize(Contest contest, Prize prize) {
        return teamRepository.findByContestId(contest.getId()).stream()
                    .sorted((t1, t2) -> Integer.compare(t2.getPoints(), t1.getPoints()))
                    .limit(prize.getNumberOfWinners())
                    .flatMap(team -> team.getMembers().stream())
                    .collect(Collectors.toSet());
    }

    @Transactional
    public void joinContest(Long contestId, JoinContestRequest joinRequest) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND));

        User user = userRepository.findById(joinRequest.getUserId())
                .orElseThrow(() -> new SolvedException(ContestErrorCode.USER_NOT_FOUND));

        ZonedDateTime startTimeKST = contest.getStartTime().atZone(ZoneId.of("Asia/Seoul"));
        ZonedDateTime endTimeKST = contest.getEndTime().atZone(ZoneId.of("Asia/Seoul"));

        if (nowKST().isBefore(startTimeKST) || nowKST().isAfter(endTimeKST)) {
            throw new SolvedException(ContestErrorCode.CONTEST_INVALID_DATE);
        }


        // 운영자는 참가자 등록 불필요
        if (contest.getOrganizers().contains(user)) {
            return;
        }

        // 이미 참가자인지 확인
        if (contest.getParticipants() != null && contest.getParticipants().contains(user)) {
            throw new SolvedException(ContestErrorCode.ALREADY_REGISTERED);
        }

        // 단체전 참가 가능 소속 검사
        if (contest.getType() == ContestType.GROUP) {
            throw new SolvedException(ContestErrorCode.INVALID_CONTEST_TYPE);
        }

        // 참가자 등록
        if (contest.getParticipants() == null) contest.setParticipants(new HashSet<>());
        contest.getParticipants().add(user);
        contestRepository.save(contest);

        // 팀 생성 or 반환
        teamService.getOrCreateTeamForContest(contestId, user, null);
    }


    @Transactional(readOnly = true)
    public boolean isUserParticipating(Long contestId, Long userId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.USER_NOT_FOUND));

        // 플랫폼 관리자 당근빠따로 접근 허용
        if (user.getRole() == Role.ROLE_ADMIN || user.getRole() == Role.ROLE_VALIDATOR) return true;
        // 대회 운영자는 참가 명단에 없어도 참가
        if (contest.getOrganizers().contains(user)) return true;


        // 일반 사용자인 경우, 참가자 명단을 확인
        if (contest.getParticipants() == null || !contest.getParticipants().contains(user)) {
            return false;
        }

        if (contest.getType() == ContestType.INDIVIDUAL) return true;

        // 팀/단체전의 경우 실제 팀에 소속되어 있어야 참가 중으로 판단
        return teamRepository.existsByContestIdAndMembersContains(contestId, user);
    }

    public List<UserContestDto> getUserContests(User user) {
        List<Contest> contests = contestRepository.findContestsByParticipant(user);

        return contests.stream().map(contest -> {
            Optional<Prize> prizeOpt = prizeRepository.findAll().stream()
                    .filter(p -> p.getContest().equals(contest)
                            && p.getWinners().contains(user))
                    .findFirst();

            boolean winner = prizeOpt.isPresent();
            int rank = winner ? prizeOpt.get().getRank() : 0;

            return UserContestDto.from(contest, winner, rank);
        }).toList();
    }

    private LocalDateTime getStartOfMonth() {
        ZonedDateTime nowKST = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        ZonedDateTime startOfMonth = nowKST
                .with(TemporalAdjusters.firstDayOfMonth())
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        return startOfMonth.toLocalDateTime();
    }



    @Transactional(readOnly = true)
    public ContestStatisticsDto getContestStatistics() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime startOfMonth = getStartOfMonth();

        int ongoingContests = contestRepository.countByStartTimeBeforeAndEndTimeAfter(now.toLocalDateTime(), now.toLocalDateTime());
        int monthlyContests = contestRepository.countByStartTimeAfter(startOfMonth);
        int monthlyParticipants = contestRepository.countDistinctParticipantsByStartTimeAfter(startOfMonth);
        int monthlyWinners = prizeRepository.countDistinctWinnersByContestEndTimeAfter(startOfMonth);

        return ContestStatisticsDto.builder()
                .ongoingContests(ongoingContests)
                .monthlyParticipants(monthlyParticipants)
                .monthlyContests(monthlyContests)
                .monthlyWinners(monthlyWinners)
                .build();
    }

    @Transactional(readOnly = true)
    public Set<Affiliation> getEligibleAffiliationsForContest(Long contestId, Long userId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.USER_NOT_FOUND));

        Set<Affiliation> userAffiliations = user.getAffiliations();
        if (userAffiliations.isEmpty()) return Collections.emptySet();

        if (contest.getType() != ContestType.GROUP) {
            return userAffiliations;
        }

        // 단체전
        if (!contest.getAffiliations().isEmpty()) {
            return userAffiliations.stream()
                    .filter(contest.getAffiliations()::contains)
                    .collect(Collectors.toSet());
        } else if (!contest.getAffiliationTypes().isEmpty()) {
            return userAffiliations.stream()
                    .filter(a -> contest.getAffiliationTypes().contains(a.getType()))
                    .collect(Collectors.toSet());
        }

        return Collections.emptySet();
    }

    @Transactional
    public void joinGroupContest(Long contestId, Long userId, Long affiliationId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.USER_NOT_FOUND));

        if (contest.getType() != ContestType.GROUP) {
            throw new SolvedException(ContestErrorCode.INVALID_CONTEST_TYPE);
        }

        ZonedDateTime startTimeKST = contest.getStartTime().atZone(ZoneId.of("Asia/Seoul"));
        ZonedDateTime endTimeKST = contest.getEndTime().atZone(ZoneId.of("Asia/Seoul"));

        if (nowKST().isBefore(startTimeKST) || nowKST().isAfter(endTimeKST)) {
            throw new SolvedException(ContestErrorCode.CONTEST_INVALID_DATE);
        }


        Affiliation chosenAffiliation = user.getAffiliations().stream()
                .filter(a -> a.getId().equals(affiliationId))
                .findFirst()
                .orElseThrow(() -> new SolvedException(ContestErrorCode.NOT_ELIGIBLE_AFFILIATION));

        Set<Affiliation> eligibleAffiliations = getEligibleAffiliationsForContest(contestId, userId);
        if (!eligibleAffiliations.contains(chosenAffiliation)) {
            throw new SolvedException(ContestErrorCode.NOT_ELIGIBLE_AFFILIATION);
        }

        if (contest.getParticipants() == null) {
            contest.setParticipants(new HashSet<>());
        }
        if (contest.getParticipants().contains(user)) {
            throw new SolvedException(ContestErrorCode.ALREADY_REGISTERED);
        }

        Team team = teamService.getOrCreateTeamForContest(contestId, user, chosenAffiliation.getName());
        teamRepository.save(team);

        contest.getParticipants().add(user);
        contestRepository.save(contest);
    }

    private void validateContestTime(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("시작 시간과 종료 시간은 반드시 입력해야 합니다.");
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        ZonedDateTime startKST = start.atZone(ZoneId.of("Asia/Seoul"));
        ZonedDateTime endKST = end.atZone(ZoneId.of("Asia/Seoul"));

        // 시작 시간은 종료 시간 이전
        if (!startKST.isBefore(endKST)) {
            throw new IllegalArgumentException("대회의 시작 시간은 종료 시간보다 반드시 이전이어야 합니다.");
        }

        // 종료 시간은 현재 시간 이후
        if (endKST.isBefore(now)) {
            throw new IllegalArgumentException("종료 시간을 현재 시간보다 이전으로 설정할 수 없습니다.");
        }
    }

    private ZonedDateTime nowKST() {
        return ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    }

}
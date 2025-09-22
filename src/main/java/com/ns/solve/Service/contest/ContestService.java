package com.ns.solve.service.contest;

import com.ns.solve.domain.dto.contest.*;
import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.contest.Prize;
import com.ns.solve.domain.entity.user.Affiliation;
import com.ns.solve.domain.entity.user.User;
import com.ns.solve.domain.vo.AffiliationType;
import com.ns.solve.domain.vo.ContestStatus;
import com.ns.solve.domain.vo.WargameKind;
import com.ns.solve.repository.AffiliationRepository;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.repository.contest.ContestRepository;
import com.ns.solve.repository.contest.PrizeRepository;
import com.ns.solve.utils.exception.ErrorCode.ContestErrorCode;
import com.ns.solve.utils.exception.SolvedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContestService {

    private final ContestRepository contestRepository;
    private final UserRepository userRepository;
    private final AffiliationRepository affiliationRepository;
    private final PrizeRepository prizeRepository;

    @Transactional
    public ContestDto createContest(RegisterContestRequest registerContestRequest) {
        if (registerContestRequest.getAffiliationIds() != null && !registerContestRequest.getAffiliationIds().isEmpty() &&
                (registerContestRequest.getAffiliationTypes() == null || registerContestRequest.getAffiliationTypes().isEmpty())) {

            throw new IllegalArgumentException("특정 소속을 지정하려면 소속 유형도 함께 선택해야 합니다.");
        }

        Set<User> organizers = userRepository.findAllById(registerContestRequest.getOrganizerIds()).stream()
                .collect(Collectors.toSet());

        List<Long> affiliationIds = registerContestRequest.getAffiliationIds() != null ? registerContestRequest.getAffiliationIds() : Collections.emptyList();
        Set<Affiliation> affiliations = affiliationRepository.findAllById(affiliationIds).stream()
                .collect(Collectors.toSet());
        Set<AffiliationType> affiliationTypes = new HashSet<>(registerContestRequest.getAffiliationTypes());

        Set<WargameKind> registeredProblmKinds = new HashSet<>(registerContestRequest.getProblemKinds());

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
                .status(ContestStatus.UPCOMING)
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
        return ContestDto.from(newContest);
    }

    @Transactional(readOnly = true)
    public ContestDto getContestById(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND));
        return ContestDto.from(contest);
    }

    @Transactional(readOnly = true)
    public List<ContestDto> getContests(ContestStatus status, String searchTerm) {
        if (searchTerm != null && !searchTerm.isBlank()) {
            List<Contest> contests = contestRepository.findByTitleContainingIgnoreCase(searchTerm);
            return contests.stream().map(ContestDto::from).collect(Collectors.toList());
        }

        if (ContestStatus.UPCOMING.equals(status)) {
            List<Contest> contests = contestRepository.findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now());
            return contests.stream().map(ContestDto::from).collect(Collectors.toList());
        } else if (ContestStatus.ONGOING.equals(status)) {
            LocalDateTime now = LocalDateTime.now();
            List<Contest> contests = contestRepository.findByStartTimeBeforeAndEndTimeAfterOrderByEndTimeAsc(now, now);
            return contests.stream().map(ContestDto::from).collect(Collectors.toList());
        } else if (ContestStatus.ENDED.equals(status)) {
            List<Contest> contests = contestRepository.findByEndTimeBeforeOrderByEndTimeDesc(LocalDateTime.now());
            return contests.stream().map(ContestDto::from).collect(Collectors.toList());
        } else {
            List<Contest> contests = contestRepository.findAll();
            return contests.stream().map(ContestDto::from).collect(Collectors.toList());
        }
    }

    @Transactional
    public ContestDto updateContest(Long contestId, ModifyContestRequest modifyContestRequest) {
        if (modifyContestRequest.getAffiliationIds() != null && !modifyContestRequest.getAffiliationIds().isEmpty() &&
                (modifyContestRequest.getAffiliationTypes() == null || modifyContestRequest.getAffiliationTypes().isEmpty())) {
            throw new IllegalArgumentException("특정 소속을 지정하려면 소속 유형도 함께 선택해야 합니다.");
        }

        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND));

        // 기존 상금 리스트를 삭제하고 새로운 리스트로 교체
        // 이 과정에서 Hibernate가 OrphanRemoval을 처리하도록 유도하거나,
        // 직접 삭제 후 새 리스트를 설정하는 방식으로 변경
        contest.getPrizes().clear(); // 기존 Prize 엔티티의 연관 관계를 끊고 삭제하도록 설정

        if (modifyContestRequest.isPrizeEnabled() && modifyContestRequest.getPrizes() != null) {
            List<Prize> newPrizes = modifyContestRequest.getPrizes().stream()
                    .map(dto -> Prize.builder()
                            .rank(dto.getRank())
                            .name(dto.getName())
                            .numberOfWinners(dto.getNumberOfWinners())
                            .build())
                    .collect(Collectors.toList());

            // 새로운 Prize 엔티티들을 Contest에 추가
            // (Contest 엔티티에 addPrize 메소드를 구현하는 것이 이상적)
            newPrizes.forEach(prize -> {
                prize.setContest(contest);
                contest.getPrizes().add(prize);
            });
        }

        // Contest 엔티티의 다른 필드들 업데이트
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
        Set<Affiliation> updatedAffiliations = affiliationRepository.findAllById(modifyContestRequest.getAffiliationIds()).stream().collect(Collectors.toSet());
        contest.setAffiliations(updatedAffiliations);

        // Contest 엔티티만 저장 (자동으로 Prize 엔티티도 함께 저장/삭제)
        Contest updatedContest = contestRepository.save(contest);
        return ContestDto.from(updatedContest);
    }

    @Transactional(readOnly = true)
    public ContestResultDto getContestResults(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND));

        if (!contest.getStatus().equals(ContestStatus.ENDED)) {
            throw new SolvedException(ContestErrorCode.CONTEST_NOT_ENDED);
        }

        return ContestResultDto.from(contest);
    }

    @Transactional
    public void joinContest(Long contestId, JoinContestRequest joinContestRequest) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND));

        User user = userRepository.findById(joinContestRequest.getUserId())
                .orElseThrow(() -> new SolvedException(ContestErrorCode.USER_NOT_FOUND));

        if (contest.getStatus() != ContestStatus.UPCOMING) {
            throw new SolvedException(ContestErrorCode.CONTEST_NOT_UPCOMING);
        }

        // 운영자인지 확인
        Set<User> contestOrganizers = contest.getOrganizers();
        boolean isOrganizer = contestOrganizers != null &&
                contestOrganizers.stream()
                        .anyMatch(organizer -> organizer.getId().equals(user.getId()));

        if (isOrganizer) {
            return;
        }

        Set<AffiliationType> allowedAffiliationTypes = contest.getAffiliationTypes();
        Set<Affiliation> allowedAffiliations = contest.getAffiliations();

        boolean isAffiliationRestricted = (allowedAffiliationTypes != null && !allowedAffiliationTypes.isEmpty())
                || (allowedAffiliations != null && !allowedAffiliations.isEmpty());

        if (isAffiliationRestricted) {
            boolean isEligible = false;

            if (user.getAffiliations() != null && !user.getAffiliations().isEmpty()) {
                // 사용자의 소속 중 하나라도 대회 참가 조건에 맞는지 확인
                for (Affiliation userAffiliation : user.getAffiliations()) {
                    if (allowedAffiliationTypes != null && allowedAffiliationTypes.contains(userAffiliation.getType())) {
                        isEligible = true;
                        break;
                    }
                    if (allowedAffiliations != null && allowedAffiliations.contains(userAffiliation)) {
                        isEligible = true;
                        break;
                    }
                }
            }

            if (!isEligible) {
                throw new SolvedException(ContestErrorCode.NOT_ELIGIBLE_AFFILIATION);
            }
        }

        // 이미 참가자로 등록되었는지
        if (contest.getParticipants() == null) {
            contest.setParticipants(new java.util.HashSet<>());
        }

        if (contest.getParticipants().contains(user)) {
            throw new SolvedException(ContestErrorCode.ALREADY_REGISTERED);
        }

        contest.getParticipants().add(user);
        contestRepository.save(contest);
    }


    @Transactional(readOnly = true)
    public boolean isUserParticipating(Long contestId, Long userId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.USER_NOT_FOUND));

        if (contest.getParticipants() == null) {
            return false;
        }

        return contest.getParticipants().contains(user);
    }
}
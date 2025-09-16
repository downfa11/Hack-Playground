package com.ns.solve.service.contest;

import com.ns.solve.domain.dto.contest.ContestDto;
import com.ns.solve.domain.dto.contest.ContestResultDto;
import com.ns.solve.domain.dto.contest.JoinContestRequest;
import com.ns.solve.domain.dto.contest.ModifyContestRequest;
import com.ns.solve.domain.dto.contest.RegisterContestRequest;
import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.contest.Prize;
import com.ns.solve.domain.entity.user.Affiliation;
import com.ns.solve.domain.entity.user.User;
import com.ns.solve.domain.vo.ContestStatus;
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
        
        Set<User> organizers = userRepository.findAllById(registerContestRequest.getOrganizerIds()).stream().collect(Collectors.toSet());
        Set<Affiliation> affiliations = affiliationRepository.findAllById(registerContestRequest.getAffiliationIds()).stream().collect(Collectors.toSet());

        Contest contest = Contest.builder()
                .title(registerContestRequest.getTitle())
                .description(registerContestRequest.getDescription())
                .startTime(registerContestRequest.getStartTime())
                .endTime(registerContestRequest.getEndTime())
                .type(registerContestRequest.getType())
                .maxTeamSize(registerContestRequest.getMaxTeamSize())
                .organizerName(registerContestRequest.getOrganizerName())
                .organizers(organizers)
                .affiliations(affiliations)
                .affiliationTypes(registerContestRequest.getAffiliationTypes())
                .prize(registerContestRequest.isPrizeEnabled() ? registerContestRequest.getPrizeMoney() : null)
                .rules(registerContestRequest.getRules())
                .reviewConsent(registerContestRequest.isReviewConsent())
                .status(ContestStatus.UPCOMING)
                .build();

        Contest newContest = contestRepository.save(contest);

        if (registerContestRequest.getPrizes() != null && registerContestRequest.isPrizeEnabled()) {
            List<Prize> prizes = registerContestRequest.getPrizes().stream()
                    .map(dto -> Prize.builder()
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

        contest.setTitle(modifyContestRequest.getTitle());
        contest.setDescription(modifyContestRequest.getDescription());
        contest.setStartTime(modifyContestRequest.getStartTime());
        contest.setEndTime(modifyContestRequest.getEndTime());
        contest.setType(modifyContestRequest.getType());
        contest.setMaxTeamSize(modifyContestRequest.getMaxTeamSize());
        contest.setOrganizerName(modifyContestRequest.getOrganizerName());
        contest.setPrize(modifyContestRequest.isPrizeEnabled() ? modifyContestRequest.getPrizeMoney() : null);
        contest.setRules(modifyContestRequest.getRules());

        Set<User> updatedOrganizers = userRepository.findAllById(modifyContestRequest.getOrganizerIds()).stream().collect(Collectors.toSet());
        contest.setOrganizers(updatedOrganizers);

        contest.setAffiliationTypes(modifyContestRequest.getAffiliationTypes());
        Set<Affiliation> updatedAffiliations = affiliationRepository.findAllById(modifyContestRequest.getAffiliationIds()).stream().collect(Collectors.toSet());
        contest.setAffiliations(updatedAffiliations);

        prizeRepository.deleteAll(contest.getPrizes());
        if (modifyContestRequest.isPrizeEnabled() && modifyContestRequest.getPrizes() != null) {
            List<Prize> newPrizes = modifyContestRequest.getPrizes().stream()
                    .map(dto -> Prize.builder()
                            .name(dto.getName())
                            .numberOfWinners(dto.getNumberOfWinners())
                            .contest(contest)
                            .build())
                    .collect(Collectors.toList());
            prizeRepository.saveAll(newPrizes);
            contest.setPrizes(newPrizes);
        }

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

        // todo. checkAffiliationEligibility(contest, user);

        if (contest.getParticipants() == null) {
            contest.setParticipants(new java.util.HashSet<>());
        }

        if (contest.getParticipants().contains(user)) {
            throw new SolvedException(ContestErrorCode.ALREADY_REGISTERED);
        }

        contest.getParticipants().add(user);
        contestRepository.save(contest);
    }
}
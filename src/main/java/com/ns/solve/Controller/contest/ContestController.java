package com.ns.solve.controller.contest;

import com.ns.solve.domain.dto.contest.*;
import com.ns.solve.domain.dto.user.AffiliationDto;
import com.ns.solve.domain.entity.user.Affiliation;
import com.ns.solve.domain.vo.ContestStatus;
import com.ns.solve.service.contest.ContestService;
import com.ns.solve.utils.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contests")
public class ContestController {

    private final ContestService contestService;

    @PostMapping
    public ResponseEntity<ContestDto> createContest(@RequestBody RegisterContestRequest registerContestRequest) {
        ContestDto newContest = contestService.createContest(registerContestRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(newContest);
    }

    @PutMapping("/{contestId}")
    public ResponseEntity<ContestDto> updateContest(@PathVariable Long contestId, @RequestBody ModifyContestRequest modifyContestRequest) {
        ContestDto updatedContest = contestService.updateContest(contestId, modifyContestRequest);
        return ResponseEntity.ok(updatedContest);
    }

    @GetMapping("/{contestId}")
    public ResponseEntity<ContestDto> getContestById(@PathVariable Long contestId) {
        ContestDto contest = contestService.getContestById(contestId);
        return ResponseEntity.ok(contest);
    }

    @GetMapping("/{contestId}/results")
    public ResponseEntity<ContestResultDto> getContestResults(@PathVariable Long contestId) {
        ContestResultDto contestResults = contestService.getContestResults(contestId);
        return ResponseEntity.ok(contestResults);
    }

    @GetMapping
    public ResponseEntity<List<ContestDto>> getAllContests(@RequestParam(required = false) ContestStatus status, @RequestParam(required = false) String searchTerm) {
        List<ContestDto> contests = contestService.getContests(status, searchTerm);
        return ResponseEntity.ok(contests);
    }

    @PostMapping("/{contestId}/join")
    public ResponseEntity<Void> joinContest(@PathVariable Long contestId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        contestService.joinContest(contestId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{contestId}/participants/{userId}")
    public ResponseEntity<Boolean> isUserParticipating(@PathVariable Long contestId, @PathVariable Long userId) {
        boolean isParticipating = contestService.isUserParticipating(contestId, userId);
        return ResponseEntity.ok(isParticipating);
    }

    @GetMapping("/statistics")
    public ResponseEntity<ContestStatisticsDto> getContestStatistics() {
        ContestStatisticsDto statistics = contestService.getContestStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/{contestId}/eligible-affiliations/{userId}")
    public ResponseEntity<Set<AffiliationDto>> getEligibleAffiliations(@PathVariable Long contestId, @PathVariable Long userId) {
        Set<Affiliation> eligibleAffiliations = contestService.getEligibleAffiliationsForContest(contestId, userId);
        Set<AffiliationDto> dtoSet = eligibleAffiliations.stream()
                .map(AffiliationDto::from)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(dtoSet);
    }

}
package com.ns.solve.controller.contest;

import com.ns.solve.domain.dto.contest.*;
import com.ns.solve.domain.vo.ContestStatus;
import com.ns.solve.service.contest.ContestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<Void> joinContest(@PathVariable Long contestId, @RequestBody JoinContestRequest joinContestRequest) {
        contestService.joinContest(contestId, joinContestRequest);
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
}
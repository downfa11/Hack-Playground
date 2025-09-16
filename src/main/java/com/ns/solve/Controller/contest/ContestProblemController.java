package com.ns.solve.controller.contest;

import com.ns.solve.domain.entity.contest.ContestProblem;
import com.ns.solve.service.contest.ContestProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contests/{contestId}/problems")
public class ContestProblemController {

    private final ContestProblemService contestProblemService;

    // todo. RequestBody를 위한 DTO 따로 생성, 반환용 Dto도 따로 필요
//    @PostMapping
//    public ResponseEntity<ContestProblem> createProblem(@PathVariable Long contestId, @RequestBody RegisterContestProblemRequest registerContestProblemRequest) {
//        ContestProblem newProblem = contestProblemService.createProblem(contestId, registerContestProblemRequest);
//        return ResponseEntity.status(HttpStatus.CREATED).body(newProblem);
//    }
//
//    @PutMapping("/{problemId}")
//    public ResponseEntity<ContestProblem> updateProblem(@PathVariable Long problemId, @RequestBody ModifyContestProblemRequest modifyContestProblemRequest) {
//        ContestProblem updatedProblem = contestProblemService.updateProblem(problemId, modifyContestProblemRequest);
//        return ResponseEntity.ok(updatedProblem);
//    }

    @DeleteMapping("/{problemId}")
    public ResponseEntity<Void> deleteProblem(@PathVariable Long problemId) {
        contestProblemService.deleteProblem(problemId);
        return ResponseEntity.noContent().build();
    }

    // todo. 문제 잠금 처리 및 해제 기능

    // -------------일반 사용자 -------------------------------------

    @GetMapping
    public ResponseEntity<List<ContestProblem>> getProblems(@PathVariable Long contestId, @RequestParam(required = false) String category, @RequestParam(required = false) String searchTerm) {
        List<ContestProblem> problems = contestProblemService.getProblems(contestId, category, searchTerm);
        return ResponseEntity.ok(problems);
    }

    // todo. ContestProblem 문제 생성이나 파일 다운로드 등 처리, 정답 여부
}

package com.ns.solve.controller.contest;

import com.ns.solve.domain.dto.MessageEntity;
import com.ns.solve.domain.dto.contest.ModifyContestProblemRequest;
import com.ns.solve.domain.dto.contest.RegisterContestProblemRequest;
import com.ns.solve.domain.entity.contest.ContestProblem;
import com.ns.solve.domain.vo.WargameKind;
import com.ns.solve.service.contest.ContestProblemService;
import com.ns.solve.utils.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contests/{contestId}/problems")
public class ContestProblemController {

    private final ContestProblemService contestProblemService;

    @PostMapping
    public ResponseEntity<ContestProblem> createProblem(@PathVariable Long contestId, @RequestBody RegisterContestProblemRequest registerContestProblemRequest) {
        ContestProblem newProblem = contestProblemService.createProblem(contestId, registerContestProblemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProblem);
    }

    @PutMapping("/{problemId}")
    public ResponseEntity<ContestProblem> updateProblem(@PathVariable Long problemId, @RequestBody ModifyContestProblemRequest modifyContestProblemRequest) {
        ContestProblem updatedProblem = contestProblemService.updateProblem(problemId, modifyContestProblemRequest);
        return ResponseEntity.ok(updatedProblem);
    }

    @DeleteMapping("/{problemId}")
    public ResponseEntity<Void> deleteProblem(@PathVariable Long problemId) {
        contestProblemService.deleteProblem(problemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{problemId}/lock")
    public ResponseEntity<Void> lockProblem(@PathVariable Long problemId) {
        contestProblemService.lockProblem(problemId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{problemId}/unlock")
    public ResponseEntity<Void> unlockProblem(@PathVariable Long problemId) {
        contestProblemService.unlockProblem(problemId);
        return ResponseEntity.ok().build();
    }

    // -------------일반 사용자 -------------------------------------

    @GetMapping
    public ResponseEntity<List<ContestProblem>> getProblems(@PathVariable Long contestId, @RequestParam(required = false) WargameKind kind, @RequestParam(required = false) String searchTerm) {
        List<ContestProblem> problems = contestProblemService.getProblems(contestId, kind, searchTerm);
        return ResponseEntity.ok(problems);
    }

    // 문제 상세 조회
    @GetMapping("/{problemId}")
    public ResponseEntity<ContestProblem> getProblemDetail(@PathVariable Long contestId, @PathVariable Long problemId) {
        ContestProblem problem = contestProblemService.getProblemDetail(contestId, problemId);
        return ResponseEntity.ok(problem);
    }

    // 문제 파일 다운로드
    @GetMapping("/{problemId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long problemId) {
        Resource fileResource = contestProblemService.downloadProblemFile(problemId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResource.getFilename() + "\"")
                .body(fileResource);
    }

    @PostMapping("/{problemId}/solve")
    public ResponseEntity<MessageEntity> solveProblem(@PathVariable Long contestId, @PathVariable Long problemId, @RequestParam String flag, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        return ResponseEntity.ok(new MessageEntity(
                "solveProblem Result", contestProblemService.solveProblem(userId, contestId, problemId, flag)));
    }
}

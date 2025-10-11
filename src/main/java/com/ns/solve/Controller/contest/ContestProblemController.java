package com.ns.solve.controller.contest;

import com.ns.solve.domain.dto.MessageEntity;
import com.ns.solve.domain.dto.contest.ContestProblemDto;
import com.ns.solve.domain.dto.contest.ModifyContestProblemRequest;
import com.ns.solve.domain.dto.contest.RegisterContestProblemRequest;
import com.ns.solve.domain.vo.WargameKind;
import com.ns.solve.service.contest.ContestProblemService;
import com.ns.solve.utils.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contests/{contestId}/problems")
public class ContestProblemController {

    private final ContestProblemService contestProblemService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContestProblemDto> createProblem(@PathVariable Long contestId, @RequestBody RegisterContestProblemRequest registerContestProblemRequest,
                                                           @RequestPart(value = "file", required = false) MultipartFile file, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        ContestProblemDto newProblem = contestProblemService.createProblem(contestId, userId, registerContestProblemRequest, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProblem);
    }

    @PutMapping(value="/{problemId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContestProblemDto> updateProblem(@PathVariable Long problemId, @RequestBody ModifyContestProblemRequest modifyContestProblemRequest,
                                                           @RequestPart(value = "file", required = false) MultipartFile file, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        ContestProblemDto updatedProblem = contestProblemService.updateProblem(problemId, userId, modifyContestProblemRequest, file);
        return ResponseEntity.ok(updatedProblem);
    }

    @DeleteMapping("/{problemId}")
    public ResponseEntity<Void> deleteProblem(@PathVariable Long problemId) {
        contestProblemService.deleteProblem(problemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{problemId}/file-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageEntity> uploadProblemFile(@PathVariable Long problemId, @RequestPart("file") MultipartFile file, Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        contestProblemService.uploadFile(problemId, userId, file);
        return ResponseEntity.ok(new MessageEntity("file upload", "File uploaded successfully."));
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
    public ResponseEntity<List<ContestProblemDto>> getProblems(@PathVariable Long contestId, @RequestParam(required = false) WargameKind kind, @RequestParam(required = false) String searchTerm, Authentication authentication) {
        Long userId = null;
        if (authentication != null) {
            userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        }
        List<ContestProblemDto> problems = contestProblemService.getProblems(contestId, kind, searchTerm, userId);
        return ResponseEntity.ok(problems);
    }

    // 문제 상세 조회
    @GetMapping("/{problemId}")
    public ResponseEntity<ContestProblemDto> getProblemDetail(@PathVariable Long contestId, @PathVariable Long problemId, Authentication authentication) {
        Long userId = null;
        if (authentication != null) {
            userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        }

        ContestProblemDto problem = contestProblemService.getProblemDetail(contestId, problemId, userId);
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

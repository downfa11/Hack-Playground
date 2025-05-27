package com.ns.solve.controller.problem;

import com.ns.solve.domain.dto.MessageEntity;
import com.ns.solve.domain.dto.board.BoardSummary;
import com.ns.solve.domain.dto.problem.ModifyProblemDto;
import com.ns.solve.domain.dto.problem.ProblemDto;
import com.ns.solve.domain.dto.problem.ProblemSummary;
import com.ns.solve.domain.dto.problem.RegisterProblemDto;
import com.ns.solve.domain.dto.problem.wargame.RegisterWargameProblemDto;
import com.ns.solve.domain.dto.user.UserFirstBloodDto;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.domain.entity.problem.ProblemType;
import com.ns.solve.domain.entity.problem.WargameKind;
import com.ns.solve.service.problem.ProblemService;
import com.ns.solve.utils.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/problems")
public class ProblemController {
    private final ProblemService problemService;

    @Operation(summary = "문제 생성", description = "새로운 문제를 생성합니다. type은 문제 종류(wargame, assignment, algorithm)이고, kind가 세부 유형(웹해킹 등..)입니다. Wargame이나 다른 종류의 문제도 추가 필드값만 넣으면 생성할 수 있습니다. ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "문제가 성공적으로 생성되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    @PostMapping
    public ResponseEntity<MessageEntity> createProblem(@RequestBody RegisterProblemDto registerProblemDto, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        if (registerProblemDto instanceof RegisterWargameProblemDto wargameDto) {
            ProblemDto createdProblem = problemService.createProblem(userId, wargameDto);
            return ResponseEntity.status(201).body(new MessageEntity("Problem created successfully", createdProblem));
        } else {
            ProblemDto createdProblem = problemService.createProblem(userId, registerProblemDto);
            return ResponseEntity.status(201).body(new MessageEntity("Problem created successfully", createdProblem));
        }
    }

    @Operation(summary = "문제 수정", description = "주어진 문제 ID로 문제를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문제가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "404", description = "문제를 찾을 수 없습니다.")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageEntity> updateProblem(@PathVariable Long id,
                                                       @RequestPart ModifyProblemDto modifyProblemDto, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        ProblemDto updatedProblem = problemService.updateProblem(userId, id, modifyProblemDto);
        return ResponseEntity.ok(new MessageEntity("Problem updated successfully", updatedProblem));
    }

    @Operation(summary = "문제 난이도 수정", description = "주어진 문제 ID로 문제를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문제가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "404", description = "문제를 찾을 수 없습니다.")
    })
    @PutMapping(value = "/{id}/level")
    public ResponseEntity<MessageEntity> updateProblem(@PathVariable Long id, @RequestParam String level, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        ProblemDto updatedProblem = problemService.updateProblemLevel(userId, id, level);
        return ResponseEntity.ok(new MessageEntity("Problem updated successfully", updatedProblem));
    }

    @Operation(summary = "모든 문제 조회", description = "모든 문제를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문제들이 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "문제를 찾을 수 없습니다.")
    })
    @GetMapping
    public ResponseEntity<MessageEntity> getAllProblems() {
        List<Problem> problems = problemService.getAllProblems();
        return ResponseEntity.ok(new MessageEntity("success", problems));
    }

    @Operation(summary = "문제 ID로 조회", description = "주어진 문제 ID로 문제를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문제가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "문제를 찾을 수 없습니다.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MessageEntity> getProblemById(@PathVariable Long id) {
        Optional<ProblemDto> problem = problemService.getProblemDtoById(id);

        if (problem.isPresent()) {
            return ResponseEntity.ok(new MessageEntity("Problem found", problem.get()));
        } else {
            return ResponseEntity.status(404).body(new MessageEntity("Problem not found", null));
        }
    }

    @Operation(summary = "문제 삭제", description = "주어진 문제 ID로 문제를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문제가 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "404", description = "문제를 찾을 수 없습니다.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageEntity> deleteProblem(@PathVariable Long id, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        problemService.deleteProblem(userId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "검수 완료된 문제 조회", description = "검수 완료된 문제들을 주어진 유형(type='WARGAME')에 따라 조회합니다. 종류(kind='WEBHACKING'...)도 구분합니다. 단, 정렬 조건(sortKind)은 마지막 수정일(updatedAt), 정답율(correctRate)로 구분하며 그 외에는 모두 문제 번호로 정렬합니다. ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "완료된 문제들이 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "완료된 문제들을 찾을 수 없습니다.")
    })
    @GetMapping("/completed")
    public ResponseEntity<Page<ProblemSummary>> getCompletedProblemsByType(
            @RequestParam Long userId,
            @RequestParam ProblemType type,
            @RequestParam(required = false) WargameKind kind,
            @RequestParam(required = false) String sortKind,
            @RequestParam boolean desc,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ProblemSummary> problemSummaries = problemService.getCompletedProblemsSummary(userId, type, kind, sortKind, desc, PageRequest.of(page, size));
        return new ResponseEntity<>(problemSummaries, HttpStatus.OK);
    }

    @Operation(summary = "문제 풀이", description = "주어진 문제에 대해 풀이 결과를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문제 풀이 결과가 성공적으로 반환되었습니다."),
            @ApiResponse(responseCode = "404", description = "문제를 찾을 수 없습니다.")
    })
    @GetMapping("/{id}/solve")
    public ResponseEntity<MessageEntity> solveProblem(@PathVariable Long id, @RequestParam String flag, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        return ResponseEntity.ok(new MessageEntity("solveProblem Result", problemService.solveProblem(userId, id, flag)));
    }

    @Operation(summary = "첫 번째 문제 풀이 사용자 조회", description = "주어진 문제 ID에 대해 가장 먼저 문제를 푼 사용자를 조회합니다., size를 통해서 1명 말고 랭킹 형식으로 조회도 가능해요.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "첫 번째 문제 풀이 사용자가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "문제를 찾을 수 없거나 풀이한 사용자가 없습니다.")
    })
    @GetMapping("/{id}/firstblood")
    public ResponseEntity<MessageEntity> firstBlood(@PathVariable Long id, @RequestParam int size) {
        List<UserFirstBloodDto> firstSolver = problemService.firstBlood(id, size);
        return ResponseEntity.ok(new MessageEntity("First blood found", firstSolver));
    }

    @PostMapping("/{problemId}/upload")
    public ResponseEntity<MessageEntity> uploadFile(@PathVariable Long problemId, @RequestPart(value = "file") MultipartFile file, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        problemService.uploadFile(userId, problemId, file);
        return ResponseEntity.ok(new MessageEntity("uploadFile successfully", userId));
    }

    @GetMapping("/{problemId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long problemId) {
        Resource fileResource = problemService.downloadProblemFile(problemId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResource.getFilename() + "\"")
                .body(fileResource);
    }

    @Operation(summary = "문제 통계 조회", description = "검수 완료된 문제 수와 최근 한 달 내 추가된 문제 수를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "통계가 성공적으로 조회되었습니다."),
    })
    @GetMapping("/statistics")
    public ResponseEntity<MessageEntity> getProblemStatistics() {
        Long checkedCount = problemService.getChekcedProblemsCount();
        Long newCount = problemService.getNewProblemsCount(LocalDateTime.now());

        return ResponseEntity.ok(new MessageEntity("successful",
                Map.of("checkedCount", checkedCount, "newCount", newCount)));
    }

    @Operation(summary = "문제 검색", description = "ProblemType(WARGAME, ASSIGNMENT, ALGORITHM)과 WargameKind(WEBHACKING, SYSTEM, REVERSING, CRYPTO), 키워드로 게시판을 검색합니다. 단, WargameType은 null로 보내면 전체 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시판 검색 결과가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "검색된 게시판이 없습니다.")})
    @GetMapping("/search")
    public ResponseEntity<MessageEntity> searchProblem(@RequestParam ProblemType type, @RequestParam(required = false) WargameKind kind, @RequestParam String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        Page<ProblemSummary> results = problemService.searchProblems(type, kind, keyword, PageRequest.of(page, size));

        if (results.hasContent()) {
            return ResponseEntity.ok(new MessageEntity("문제 검색 성공", results));
        } else {
            return new ResponseEntity<>(new MessageEntity("검색된 문제가 없습니다.", null), HttpStatus.NOT_FOUND);
        }
    }
}

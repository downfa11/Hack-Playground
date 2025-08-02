package com.ns.solve.controller.problem;

import com.ns.solve.domain.dto.MessageEntity;
import com.ns.solve.domain.dto.problem.ProblemDto;
import com.ns.solve.domain.dto.problem.wargame.ModifyWargameProblemDto;
import com.ns.solve.domain.dto.problem.wargame.RegisterWargameProblemDto;
import com.ns.solve.domain.dto.problem.wargame.WargameProblemDto;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.domain.entity.problem.WargameProblem;
import com.ns.solve.service.problem.ProblemService;
import com.ns.solve.utils.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wargame-problems")
public class WargameProblemController {
    private final ProblemService problemService;

    @Operation(summary = "문제 생성", description = "새로운 문제를 생성합니다. type은 문제 종류(wargame, assignment, algorithm)이고, kind가 세부 유형(웹해킹 등..)입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "문제가 성공적으로 생성되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageEntity> createProblem(@RequestPart(value = "data") RegisterWargameProblemDto registerProblemDto,
                                                       @RequestPart(value = "file", required = false) MultipartFile file, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        ProblemDto createdProblem = problemService.createProblemWithFile(userId, registerProblemDto, file);
        return ResponseEntity.status(201).body(new MessageEntity("Problem created successfully", createdProblem));
    }

    @Operation(summary = "문제 수정", description = "주어진 문제 ID로 문제를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문제가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "404", description = "문제를 찾을 수 없습니다.")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageEntity> updateProblem(@PathVariable Long id,
                                                       @RequestPart("data") ModifyWargameProblemDto modifyProblemDto,
                                                       @RequestPart(value = "file", required = false) MultipartFile file, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        ProblemDto updatedProblem = problemService.updateProblemWithFile(userId, id, modifyProblemDto, file);
        return ResponseEntity.ok(new MessageEntity("Problem updated successfully", updatedProblem));
    }

    @Operation(summary = "문제 ID로 조회", description = "주어진 문제 ID로 문제를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문제가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "문제를 찾을 수 없습니다.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MessageEntity> getWargameProblemById(@PathVariable Long id) {
        WargameProblemDto wargameProblemDto = problemService.getWargameProblemById(id);
        return ResponseEntity.ok(new MessageEntity("Wargame Problem found", wargameProblemDto));
    }

    @Operation(summary = "검수 완료된 문제 조회", description = "검수 완료된 문제들을 주어진 유형에 따라 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "완료된 문제들이 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "완료된 문제들을 찾을 수 없습니다.")
    })
    @GetMapping
    public ResponseEntity<MessageEntity> getAllWargameProblems() {
        List<WargameProblem> wargameProblems = problemService.getAllWargameProblems();
        return ResponseEntity.ok(new MessageEntity("Wargame Problems fetched successfully", wargameProblems));
    }

    @Operation(summary = "작성자 본인 문제 조회", description = "작성자 본인만 조회할 수 있는 문제 상세 정보를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문제가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "문제를 찾을 수 없습니다.")
    })
    @GetMapping("/written/{id}")
    public ResponseEntity<MessageEntity> getWrittenProblemById(@PathVariable Long id, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        return ResponseEntity.ok(new MessageEntity("작성자 본인 문제 조회 성공", problemService.getWrittenWargameProblemById(userId, id)));
    }

}

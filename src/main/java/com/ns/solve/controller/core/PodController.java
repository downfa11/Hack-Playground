package com.ns.solve.controller.core;

import com.ns.solve.domain.dto.problem.SolveInfo;
import com.ns.solve.service.core.PodService;
import com.ns.solve.utils.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pods")
public class PodController {

    private final PodService podService;

    @Operation(summary = "문제 풀이용 Pod 생성", description = "사용자와 문제 ID를 기반으로 문제 풀이용 Pod를 생성하거나 실행 중인 Pod를 재활용합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pod URL 반환 (정상 생성 또는 기존 Pod 활용)"),
            @ApiResponse(responseCode = "500", description = "Pod 생성 또는 상태 확인 중 오류 발생")
    })
    @PostMapping("/create")
    public ResponseEntity<String> createPod(@RequestParam Long problemId,  Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        return ResponseEntity.ok(podService.createProblemPod(userId, problemId));
    }

    @Operation(summary = "문제 풀이용 Pod 삭제", description = "사용자와 문제 ID를 기반으로 문제 풀이용 Pod를 삭제한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pod 삭제"),
            @ApiResponse(responseCode = "500", description = "Pod 삭제 또는 상태 확인 중 오류 발생")
    })
    @PostMapping("/delete")
    public ResponseEntity<String> deletePod(@RequestParam Long problemId,  Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        return ResponseEntity.ok(podService.deleteProblemPod(userId, problemId));
    }

    @Operation(summary = "현재 Dedicated 문제 풀이중인 사용자 목록 조회", description = "namespace는 wargame으로 고정. 현재 문제 풀이중인 사용자-문제 매핑을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 ID -> 문제 ID 매핑 반환")
    })
    @GetMapping("/active")
    public ResponseEntity<List<SolveInfo>> getCurrentSolveMembers(@RequestParam String namespace) {
        return ResponseEntity.ok(podService.findCurrentSolveMembers(namespace));
    }
}

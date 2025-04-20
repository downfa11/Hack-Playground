package com.ns.solve.controller.core;

import com.ns.solve.service.core.PodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> createPod(@RequestParam Long userId, @RequestParam Long problemId) {
        String url = podService.createProblemPod(userId, problemId);
        return url != null ?
                ResponseEntity.ok(url) :
                ResponseEntity.internalServerError().body("Create pod Failed");
    }

    @Operation(summary = "Pod 외부 노출", description = "문제에 해당하는 Pod를 외부에 노출합니다. 웹 문제일 경우 Ingress도 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "외부 접속 가능한 URL 반환"),
            @ApiResponse(responseCode = "500", description = "Service 또는 Ingress 생성 실패")
    })
    @PostMapping("/expose")
    public ResponseEntity<String> exposePod(@RequestParam Long userId,
                                            @RequestParam Long problemId,
                                            @RequestParam boolean isWebProblem) {
        String url = podService.exposePod(userId, problemId, isWebProblem);
        return url != null ? ResponseEntity.ok(url) : ResponseEntity.internalServerError().body("ExposePod Failed.");
    }

    @Operation(summary = "현재 Dedicated 문제 풀이중인 사용자 목록 조회", description = "app=solve, type=dedicated 라벨을 가진 Pod를 기반으로 현재 문제 풀이중인 사용자-문제 매핑을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 ID -> 문제 ID 매핑 반환")
    })
    @GetMapping("/active")
    public ResponseEntity<Map<String, String>> getCurrentSolveMembers() {
        return ResponseEntity.ok(podService.findCurrentSolveMember());
    }
}

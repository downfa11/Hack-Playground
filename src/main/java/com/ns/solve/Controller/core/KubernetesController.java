package com.ns.solve.controller.core;

import com.ns.solve.service.core.KubernetesAdapter;
import com.ns.solve.service.core.PodBuilder;
import com.ns.solve.service.core.PodService;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Kubernetes Adapter Test API", description = "KubernetesAdapter 기능 점검용 API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/k8s")
@Slf4j
public class KubernetesController {

    private final KubernetesAdapter kubernetesAdapter;
    private final PodService podService;

    @Operation(summary = "Pod 생성", description = "지정한 이름과 이미지로 Pod를 생성합니다.")
    @PostMapping("/pod")
    public V1Pod createPod(
            @Parameter(description = "생성할 사용자") @RequestParam Long userId,
            @Parameter(description = "생성할 Problem 번호") @RequestParam Long problemId,
            @Parameter(description = "생성할 Pod의 namespace") @RequestParam String namespace,
            @Parameter(description = "사용할 Docker 이미지") @RequestParam String image) throws ApiException {
        return kubernetesAdapter.createPod(problemId, userId, namespace, image, null);
    }

    @Operation(summary = "Pod 삭제", description = "지정한 이름의 Pod를 삭제합니다.")
    @DeleteMapping("/pod")
    public void deletePod(@Parameter(description = "삭제할 Pod 이름") @RequestParam String podName,
                          @Parameter(description = "생성할 Pod의 namespace") @RequestParam String namespace) throws ApiException {
        kubernetesAdapter.deletePod(namespace, podName);
    }

    @Operation(summary = "Pod 목록 조회", description = "현재 네임스페이스의 모든 Pod를 반환합니다.")
    @GetMapping("/pods")
    public V1PodList getPodList(@Parameter(description = "생성할 Pod의 namespace") @RequestParam String namespace) throws ApiException {
        return kubernetesAdapter.getPodList(namespace);
    }

    @Operation(summary = "Pod 상태 조회", description = "지정한 Pod의 phase 상태를 반환합니다.")
    @GetMapping("/pod/status")
    public ResponseEntity<String> getPodPhase(@Parameter(description = "조회할 Pod 이름") @RequestParam String podName,
                                              @Parameter(description = "생성할 Pod의 namespace") @RequestParam String namespace) {
        try {
            String phase = kubernetesAdapter.getPodPhase(namespace, podName)
                    .orElse("Unknown");
            return ResponseEntity.ok(phase);
        } catch (Exception e) {
            log.error("getPodPhase - fetching error  pod status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @Operation(summary = "Pod Ready 여부 확인", description = "지정한 Pod가 Ready 상태인지 확인합니다.")
    @GetMapping("/pod/ready")
    public ResponseEntity<Boolean> isPodReady(
            @Parameter(description = "확인할 Pod 이름") @RequestParam String podName,
            @Parameter(description = "생성할 Pod의 namespace") @RequestParam String namespace) {
        return ResponseEntity.ok(kubernetesAdapter.isPodReady(namespace, podName));
    }

    @Operation(summary = "Pod 로그 조회", description = "지정한 Pod의 로그를 반환합니다.")
    @GetMapping("/pods/{podName}/logs")
    public String getLogs(
            @Parameter(description = "로그를 조회할 Pod 이름") @PathVariable String podName,
            @Parameter(description = "생성할 Pod의 namespace") @RequestParam String namespace) {
        return kubernetesAdapter.getPodLogs(namespace, podName);
    }

    @Operation(summary = "Pod 내 명령어 실행", description = "지정한 Pod 내부에서 명령어를 실행합니다.")
    @PostMapping("/pods/{podName}/exec")
    public String execCommand(
            @Parameter(description = "명령어를 실행할 Pod 이름") @PathVariable String podName,
            @Parameter(description = "입력값 (stdin)") @RequestParam String input,
            @Parameter(description = "실행할 명령어 (띄어쓰기로 구분)") @RequestParam String command) {
        return kubernetesAdapter.executeCommand(podName, command.split(" "));
    }

    @Operation(summary = "Pod Ready 대기", description = "지정한 시간 동안 Pod가 Ready 상태가 될 때까지 대기합니다.")
    @GetMapping("/pod/wait")
    public ResponseEntity<Boolean> waitPodReady(@Parameter(description = "대상 Pod 이름") @RequestParam String podName,
                                                @Parameter(description = "생성할 Pod의 namespace") @RequestParam String namespace,
                                                @Parameter(description = "최대 대기 시간(초), 기본값: 30") @RequestParam(defaultValue = "30") int timeoutSeconds) {
        return ResponseEntity.ok(kubernetesAdapter.waitPodToReady(namespace, podName, timeoutSeconds));
    }

    @Operation(summary = "IngressRoute 생성", description = "Traefik용 IngressRoute를 생성합니다.")
    @PostMapping("/ingress")
    public void createIngressRoute(@Parameter(description = "생성할 Pod의 namespace") @RequestParam String namespace,
                                   @Parameter(description = "IngressRoute 정의 JSON") @RequestBody Map<String, Object> ingressRoute) throws ApiException {
        kubernetesAdapter.createIngressRoute(namespace, ingressRoute);
    }

    @Operation(summary = "Namespace 생성", description = "지정한 이름으로 Kubernetes Namespace를 생성합니다.")
    @PostMapping("/namespace")
    public ResponseEntity<String> createNamespace(
            @Parameter(description = "생성할 Namespace 이름") @RequestParam String namespaceName) {
        try {
            kubernetesAdapter.createNamespace(namespaceName);
            return ResponseEntity.ok("Namespace created successfully: " + namespaceName);
        } catch (ApiException e) {
            log.error("Namespace 생성 실패: {}", e.getResponseBody());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Namespace 생성 실패: " + e.getResponseBody());
        }
    }

}

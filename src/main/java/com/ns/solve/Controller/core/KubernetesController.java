package com.ns.solve.controller.core;

import com.ns.solve.service.core.KubernetesService;
import com.ns.solve.service.core.PodBuilder;
import com.ns.solve.service.core.PodService;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Kubernetes Adapter Test API", description = "KubernetesAdapter 기능 점검용 API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/k8s")
@Slf4j
public class KubernetesController {
    private final KubernetesService kubernetesService;

    @Operation(summary = "Pod 생성", description = "지정한 이름과 이미지로 Pod를 생성합니다.")
    @PostMapping("/pod")
    public V1Pod createPod(
            @Parameter(description = "생성할 사용자") @RequestParam Long userId,
            @Parameter(description = "생성할 Problem 번호") @RequestParam Long problemId,
            @Parameter(description = "생성할 Pod의 namespace") @RequestParam String namespace,
            @Parameter(description = "사용할 Docker 이미지") @RequestParam String image) throws ApiException {
        return kubernetesService.createPod(problemId, userId, namespace, image, null);
    }

    @Operation(summary = "Pod 삭제", description = "지정한 이름의 Pod를 삭제합니다.")
    @DeleteMapping("/pod")
    public void deletePod(@Parameter(description = "삭제할 Pod 이름") @RequestParam String podName,
                          @Parameter(description = "생성할 Pod의 namespace") @RequestParam String namespace) throws ApiException {
        kubernetesService.deletePod(namespace, podName);
    }

    @Operation(summary = "Pod 강제 삭제", description = "지정한 이름의 Pod를 강제로 삭제합니다.")
    @DeleteMapping("/pod/force")
    public void forceDeletePod(
            @Parameter(description = "삭제할 Pod 이름") @RequestParam String podName,
            @Parameter(description = "Pod가 위치한 namespace") @RequestParam String namespace) throws ApiException {
        kubernetesService.forceDeletePod(namespace, podName);
    }


    @Operation(summary = "Pod 목록 조회", description = "현재 네임스페이스의 모든 Pod를 반환합니다.")
    @GetMapping("/pods")
    public V1PodList getPodList(@Parameter(description = "생성할 Pod의 namespace") @RequestParam String namespace) throws ApiException {
        return kubernetesService.getPodList(namespace);
    }

    @Operation(summary = "Pod 상태 조회", description = "지정한 Pod의 phase 상태를 반환합니다.")
    @GetMapping("/pod/status")
    public ResponseEntity<String> getPodPhase(@Parameter(description = "조회할 Pod 이름") @RequestParam String podName,
                                              @Parameter(description = "생성할 Pod의 namespace") @RequestParam String namespace) {
        try {
            String phase = kubernetesService.getPodPhase(namespace, podName)
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
        return ResponseEntity.ok(kubernetesService.isPodReady(namespace, podName));
    }

    @Operation(summary = "Pod 로그 조회", description = "지정한 Pod의 로그를 반환합니다.")
    @GetMapping("/pods/{podName}/logs")
    public String getLogs(
            @Parameter(description = "로그를 조회할 Pod 이름") @PathVariable String podName,
            @Parameter(description = "생성할 Pod의 namespace") @RequestParam String namespace,
            @Parameter(description = "생성할 Pod의 Container") @RequestParam String containerName) {
        return kubernetesService.getPodLogs(namespace, podName, containerName);
    }

    @Operation(summary = "Pod 내 명령어 실행", description = "지정한 Pod 내부에서 명령어를 실행합니다.")
    @PostMapping("/pods/{podName}/exec")
    public String execCommand(
            @Parameter(description = "명령어를 실행할 Pod 이름") @PathVariable String podName,
            @Parameter(description = "입력값 (stdin)") @RequestParam String input,
            @Parameter(description = "실행할 명령어 (띄어쓰기로 구분)") @RequestParam String command) {
        return kubernetesService.executeCommand(podName, command.split(" "));
    }

    @Operation(summary = "Pod Ready 대기", description = "지정한 시간 동안 Pod가 Ready 상태가 될 때까지 대기합니다.")
    @GetMapping("/pod/wait")
    public ResponseEntity<Boolean> waitPodReady(@Parameter(description = "대상 Pod 이름") @RequestParam String podName,
                                                @Parameter(description = "생성할 Pod의 namespace") @RequestParam String namespace,
                                                @Parameter(description = "최대 대기 시간(초), 기본값: 30") @RequestParam(defaultValue = "30") int timeoutSeconds) {
        return ResponseEntity.ok(kubernetesService.waitPodToReady(namespace, podName, timeoutSeconds));
    }

    @Operation(summary = "Service 목록 조회", description = "지정한 네임스페이스의 Kubernetes Service 목록을 반환합니다.")
    @GetMapping("/services")
    public ResponseEntity<List<Map<String, Object>>> getServiceList(@RequestParam String namespace) {
        try {
            V1ServiceList serviceList = kubernetesService.getServiceList(namespace);
            List<Map<String, Object>> simplifiedList = serviceList.getItems().stream().map(service -> {
                Map<String, Object> item = new HashMap<>();
                item.put("name", service.getMetadata().getName());
                item.put("labels", service.getMetadata().getLabels());

                List<Map<String, Object>> ports = service.getSpec().getPorts().stream().map(port -> {
                    Map<String, Object> portInfo = new HashMap<>();
                    portInfo.put("port", String.valueOf(port.getPort()));
                    portInfo.put("targetPort", String.valueOf(port.getTargetPort()));
                    portInfo.put("protocol", port.getProtocol());
                    return portInfo;
                }).collect(Collectors.toList());
                item.put("ports", ports);

                item.put("selector", service.getSpec().getSelector());
                return item;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(simplifiedList);
        } catch (ApiException e) {
            log.error("getServiceList - 서비스 목록 조회 실패: {}", e.getResponseBody(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @Operation(summary = "Service 삭제", description = "지정한 이름의 Kubernetes Service를 삭제합니다.")
    @DeleteMapping("/service")
    public ResponseEntity<String> deleteService(
            @Parameter(description = "삭제할 Service 이름") @RequestParam String serviceName,
            @Parameter(description = "Service가 위치한 namespace") @RequestParam String namespace) {
        try {
            kubernetesService.deleteService(namespace, serviceName);
            return ResponseEntity.ok("Service 삭제 성공: " + serviceName);
        } catch (ApiException e) {
            log.error("deleteService 실패: {}", e.getResponseBody(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Service 삭제 실패: " + e.getResponseBody());
        }
    }

    @Operation(summary = "IngressRoute 생성", description = "Traefik용 IngressRoute를 생성합니다.")
    @PostMapping("/ingress")
    public void createIngressRoute(@Parameter(description = "생성할 Pod의 namespace") @RequestParam String namespace,
                                   @Parameter(description = "IngressRoute 정의 JSON") @RequestBody Map<String, Object> ingressRoute) throws ApiException {
        kubernetesService.createIngressRoute(namespace, ingressRoute);
    }

    @Operation(summary = "IngressRoute 목록 조회", description = "지정한 네임스페이스의 Kubernetes IngressRoute 목록을 반환합니다.")
    @GetMapping("/ingressRoute")
    public ResponseEntity<Map<String, Object>> getIngressRouteList(@Parameter(description = "대상 네임스페이스") @RequestParam String namespace) {
        try {
            Map<String, Object> ingressRouteList = kubernetesService.getIngressRouteList(namespace);
            return ResponseEntity.ok(ingressRouteList);
        } catch (ApiException e) {
            log.error("getServiceList - 서비스 목록 조회 실패: {}", e.getResponseBody(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "IngressRoute 삭제", description = "지정한 이름의 Traefik IngressRoute를 삭제합니다.")
    @DeleteMapping("/ingress")
    public ResponseEntity<String> deleteIngressRoute(
            @Parameter(description = "삭제할 IngressRoute 이름") @RequestParam String ingressRouteName,
            @Parameter(description = "IngressRoute가 위치한 namespace") @RequestParam String namespace) {
        try {
            kubernetesService.deleteIngressRoute(namespace, ingressRouteName);
            return ResponseEntity.ok("IngressRoute 삭제 성공: " + ingressRouteName);
        } catch (ApiException e) {
            log.error("deleteIngressRoute 실패: {}", e.getResponseBody(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("IngressRoute 삭제 실패: " + e.getResponseBody());
        }
    }

    @Operation(summary = "Namespace 생성", description = "지정한 이름으로 Kubernetes Namespace를 생성합니다.")
    @PostMapping("/namespace")
    public ResponseEntity<String> createNamespace(@Parameter(description = "생성할 Namespace 이름") @RequestParam String namespaceName) {
        try {
            kubernetesService.createNamespace(namespaceName);
            return ResponseEntity.ok("Namespace created successfully: " + namespaceName);
        } catch (ApiException e) {
            log.error("Namespace 생성 실패: {}", e.getResponseBody());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Namespace 생성 실패: " + e.getResponseBody());
        }
    }

    @GetMapping("/middleware")
    @Operation(summary = "Traefik 미들웨어 목록 조회", description = "네임스페이스 내 Traefik 미들웨어를 조회하며, labelSelector로 필터링할 수 있습니다.")
    public ResponseEntity<List<Map<String, Object>>> getAllMiddlewares(@RequestParam String namespace, @RequestParam(required = false) String labelSelector) {
        try {
            List<Map<String, Object>> middlewares = kubernetesService.listAllMiddlewares(namespace, labelSelector);
            return ResponseEntity.ok(middlewares);
        } catch (ApiException e) {
            log.error("Traefik 미들웨어 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(Map.of("error", e.getMessage())));
        }
    }

    @Operation(summary = "리소스 일괄 삭제", description = "지정한 레이블 선택자로 모든 Kubernetes 리소스를 일괄 삭제합니다.")
    @DeleteMapping("/resources")
    public ResponseEntity<String> deleteAllResourcesByLabel(
            @Parameter(description = "리소스를 삭제할 네임스페이스") @RequestParam String namespace,
            @Parameter(description = "리소스를 선택할 레이블 선택자") @RequestParam String labelSelector) {
        try {
            kubernetesService.deleteAllResourcesByLabel(namespace, labelSelector);
            return ResponseEntity.ok("모든 리소스 삭제 완료 - labelSelector: " + labelSelector);
        } catch (ApiException e) {
            log.error("리소스 삭제 실패 - labelSelector: {}", labelSelector, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("리소스 삭제 실패 - labelSelector: " + labelSelector + " - " + e.getMessage());
        }
    }

}

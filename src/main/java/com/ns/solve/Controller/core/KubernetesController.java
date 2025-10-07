package com.ns.solve.controller.core;

import com.ns.solve.domain.vo.BoardType;
import com.ns.solve.domain.vo.WargameKind;
import com.ns.solve.service.core.KubernetesService;
import com.ns.solve.service.core.PodBuilder;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.ns.solve.service.core.PodBuilder.getPodName;

@Tag(name = "Kubernetes Adapter Test API", description = "KubernetesAdapter 기능 점검용 API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/k8s")
@Slf4j
public class KubernetesController {

    private final KubernetesService kubernetesService;

    @Operation(summary = "Pod 생성", description = "Service 먼저 생성 후 Pod를 생성합니다.")
    @PostMapping("/pod")
    public V1Pod createPod(
            @RequestParam Long userId,
            @RequestParam Long problemId,
            @RequestParam WargameKind kind,
            @RequestParam Integer port,
            @RequestParam String namespace,
            @RequestParam String image) throws ApiException {

        V1Service service = PodBuilder.buildService(userId, problemId, kind, port);
        kubernetesService.createService(namespace, service);

        String serviceName = service.getMetadata().getName();
        V1Service createdService = kubernetesService.getService(namespace, serviceName);
        Integer nodePort = createdService.getSpec().getPorts().get(0).getNodePort();

        Map<String, Integer> resourceLimits = null;
        return kubernetesService.createPod(
                userId,
                problemId,
                port,
                nodePort,
                kind,
                namespace,
                image,
                resourceLimits
        );
    }

    @DeleteMapping("/pod")
    public void deletePod(@RequestParam String podName,
                          @RequestParam String namespace) throws ApiException {
        kubernetesService.deletePod(namespace, podName);
    }

    @DeleteMapping("/pod/force")
    public void forceDeletePod(@RequestParam String podName,
                               @RequestParam String namespace) throws ApiException {
        kubernetesService.forceDeletePod(namespace, podName);
    }

    @GetMapping("/pods")
    public V1PodList getPodList(@RequestParam String namespace) throws ApiException {
        return kubernetesService.getPodList(namespace);
    }

    @GetMapping("/pod/status")
    public ResponseEntity<String> getPodPhase(@RequestParam String podName,
                                              @RequestParam String namespace) {
        try {
            String phase = kubernetesService.getPodPhase(namespace, podName)
                    .orElse("Unknown");
            return ResponseEntity.ok(phase);
        } catch (Exception e) {
            log.error("getPodPhase - fetching error  pod status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/pod/ready")
    public ResponseEntity<Boolean> isPodReady(@RequestParam String podName,
                                              @RequestParam String namespace) {
        return ResponseEntity.ok(kubernetesService.isPodReady(namespace, podName));
    }

    @GetMapping("/pods/{podName}/logs")
    public String getLogs(@PathVariable String podName,
                          @RequestParam String namespace,
                          @RequestParam String containerName) {
        return kubernetesService.getPodLogs(namespace, podName, containerName);
    }

    @PostMapping("/pods/{podName}/exec")
    public String execCommand(@PathVariable String podName,
                              @RequestParam String input,
                              @RequestParam String command) {
        return kubernetesService.executeCommand(podName, command.split(" "));
    }

    @GetMapping("/pod/wait")
    public ResponseEntity<Boolean> waitPodReady(@RequestParam String podName,
                                                @RequestParam String namespace,
                                                @RequestParam(defaultValue = "30") int timeoutSeconds) {
        return ResponseEntity.ok(kubernetesService.waitPodToReady(namespace, podName, timeoutSeconds));
    }

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
                    portInfo.put("port", port.getPort());
                    portInfo.put("targetPort", port.getTargetPort());
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

    @DeleteMapping("/service")
    public ResponseEntity<String> deleteService(@RequestParam String serviceName,
                                                @RequestParam String namespace) {
        try {
            kubernetesService.deleteService(namespace, serviceName);
            return ResponseEntity.ok("Service 삭제 성공: " + serviceName);
        } catch (ApiException e) {
            log.error("deleteService 실패: {}", e.getResponseBody(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Service 삭제 실패: " + e.getResponseBody());
        }
    }

    @PostMapping("/ingress")
    public void createIngressRoute(@RequestParam String namespace,
                                   @RequestBody Map<String, Object> ingressRoute) throws ApiException {
        kubernetesService.createIngressRoute(namespace, ingressRoute);
    }

    @GetMapping("/ingressRoute")
    public ResponseEntity<Map<String, Object>> getIngressRouteList(@RequestParam String namespace) {
        try {
            Map<String, Object> ingressRouteList = kubernetesService.getIngressRouteList(namespace);
            return ResponseEntity.ok(ingressRouteList);
        } catch (ApiException e) {
            log.error("getIngressRouteList 실패: {}", e.getResponseBody(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/ingress")
    public ResponseEntity<String> deleteIngressRoute(@RequestParam String ingressRouteName,
                                                     @RequestParam String namespace) {
        try {
            kubernetesService.deleteIngressRoute(namespace, ingressRouteName);
            return ResponseEntity.ok("IngressRoute 삭제 성공: " + ingressRouteName);
        } catch (ApiException e) {
            log.error("deleteIngressRoute 실패: {}", e.getResponseBody(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("IngressRoute 삭제 실패: " + e.getResponseBody());
        }
    }

    @PostMapping("/namespace")
    public ResponseEntity<String> createNamespace(@RequestParam String namespaceName) {
        try {
            kubernetesService.createNamespace(namespaceName);
            return ResponseEntity.ok("Namespace created successfully: " + namespaceName);
        } catch (ApiException e) {
            log.error("Namespace 생성 실패: {}", e.getResponseBody());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Namespace 생성 실패: " + e.getResponseBody());
        }
    }

}

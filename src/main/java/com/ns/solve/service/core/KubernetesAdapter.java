package com.ns.solve.service.core;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class KubernetesAdapter {
    public static final String NAMESPACE = "default";
    public static final String TRAEFIK_LOG_PATH = "/var/log/traefik/access.log";

    private final CoreV1Api coreApi;
    private final CustomObjectsApi customObjectsApi;


    // 특정 Pod 생성
    public V1Pod createPod(String podName, String image, List<String> command, List<String> args) throws ApiException {
        try {
            V1Pod pod = new V1Pod()
                    .metadata(new V1ObjectMeta()
                            .name(podName)
                            .namespace(NAMESPACE)
                        .labels(Map.of("app", podName)))
                    .spec(PodBuilder.buildPodSpec(podName, image, command, args));
            return coreApi.createNamespacedPod(NAMESPACE, pod, null, null, null, null);
        } catch (ApiException e) {
            log.error("createPod 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
            throw e;
        }
    }

    // 특정 Pod을 삭제
    public void deletePod(String podName) throws ApiException {
        try {
            coreApi.deleteNamespacedPod(podName, NAMESPACE, null, null, null, null, null, null);
        } catch (ApiException e) {
            log.error("deletePod 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
            throw e;
        }
    }

    // NAMESPACE 내의 전체 Pod 목록 반환
    public V1PodList getPodList() throws ApiException {
        try {
            return coreApi.listNamespacedPod(NAMESPACE, null, null, null, null, null, null, null, null, null, null);
        } catch (ApiException e) {
            log.error("getPodList 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
            throw e;
        }
    }

    // 특정 Pod 조회 (V1Pod)
    public V1Pod getPod(String podName) throws ApiException {
        try {
            return coreApi.readNamespacedPod(podName, NAMESPACE, null);
        } catch (ApiException e) {
            log.error("getPod 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
            throw e;
        }
    }

    // 기존 Pod 갱신
    public void replacePod(V1Pod pod) throws ApiException {
        coreApi.replaceNamespacedPod(pod.getMetadata().getName(), NAMESPACE, pod, null, null, null, null);
    }

    // Kubernetes Service 생성
    public void createService(V1Service service) throws ApiException {
        try {
            coreApi.createNamespacedService(NAMESPACE, service, null, null, null, null);
        } catch (ApiException e) {
            log.error("createService 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
            throw e;
        }
    }

    // Traefik IngressRoute 생성
    public void createIngressRoute(Map<String, Object> ingressRoute) throws ApiException {
        try {
            customObjectsApi.createNamespacedCustomObject("traefik.io", "v1alpha1", NAMESPACE, "ingressroutes", ingressRoute, null, null, null);
        } catch (ApiException e) {
            log.error("createIngressRoute 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
            throw e;
        }
    }

    // labelSelector 조건에 맞는 Pod 목록을 반환
    public V1PodList getPodsByLabelSelector(String labelSelector) throws ApiException {
        try {
            return coreApi.listNamespacedPod(NAMESPACE, null, null, null, null, labelSelector, null, null, null, null, null);
        } catch (ApiException e) {
            log.error("getPodsByLabelSelector 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
            throw e;
        }
    }

    // 특정 Pod의 상태(phase)를 반환
    public Optional<String> getPodPhase(String podName) {
        try {
            V1Pod pod = getPod(podName);
            return Optional.ofNullable(pod.getStatus().getPhase());
        } catch (Exception e) {
            log.warn("Could not get phase for pod {}: {}", podName, e.getMessage());
            return Optional.empty();
        }
    }

    // 특정 Pod의 Log를 반환
    public String getPodLogs(String podName) {
        try {
            return coreApi.readNamespacedPodLog(podName, NAMESPACE, null, null, null, null, null, null, null, null, null);
        } catch (ApiException e) {
            log.error("Failed to get logs for pod {}: {}", podName, e.getResponseBody());
            return "Log fetch failed: " + e.getMessage();
        }
    }

    // 특정 Pod이 Ready 상태인지 확인
    public boolean isPodReady(String podName) {
        try {
            V1Pod pod = getPod(podName);
            return pod.getStatus().getConditions().stream()
                    .anyMatch(cond -> "Ready".equals(cond.getType()) && "True".equals(cond.getStatus()));
        } catch (Exception e) {
            log.warn("Failed to check readiness of pod {}: {}", podName, e.getMessage());
            return false;
        }
    }

    // 주어진 시간동안 Pod이 Ready 상태 될때까지 대기
    public boolean waitPodToReady(String podName, int timeoutSeconds) {
        int waited = 0;
        int interval = 2;

        while (waited < timeoutSeconds) {
            try {
                if (isPodReady(podName)) return true;
                Thread.sleep(interval * 1000L);
                waited += interval;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while waiting for pod {} to be ready", podName);
                return false;
            }
        }

        log.warn("Timeout waiting for pod {} to be ready", podName);
        return false;
    }

    // 해당 Pod에게 input 입력값 실행, command는 추가 명령어

    public String executeCommand(String podName, String... command) {
        List<String> commandList = new ArrayList<>();
        commandList.add("kubectl");
        commandList.add("exec");
        commandList.add(podName);
        commandList.add("--");
        commandList.addAll(Arrays.asList(command));

        ProcessBuilder processBuilder = new ProcessBuilder(commandList);

        try {
            Process process = processBuilder.start();
            try (Scanner scanner = new Scanner(process.getInputStream())) {
                String output = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                System.out.println("executeCommand: " + output);
                return output;
            }
        } catch (IOException e) {
            System.out.println("Error executing command on pod: " + podName + " " + e.getMessage() + " " + e);
            return "Error executing command: " + e.getMessage();
        }
    }


    // Traefik Ingress Route 스타일의 External URL 반환
    public String getExternalUrl(String podName, boolean shared) {
        return shared ? "https://shared-" + podName + ".solve.your-domain.com" : "https://" + podName + ".solve.your-domain.com";
    }

    // Traefik의 AccessLog 읽기
    public Optional<Long> getLatestRequestTimestamp(String podName) {
        try {
            String logContent = executeCommand(podName, "cat", TRAEFIK_LOG_PATH);
            return extractLatestTimestampFromLogs(logContent);
        } catch (Exception e) {
            log.error("Failed to read Traefik logs from pod {}: {}", podName, e.getMessage());
            return Optional.empty();
        }
    }

    // Traefik 로그에서 최신 요청 시간 추출
    private Optional<Long> extractLatestTimestampFromLogs(String logContent) {
        try {
            String[] logLines = logContent.split("\n");
            long latestTimestamp = 0;
            for (String logLine : logLines) {
                String timeStr = extractTimeFromLogLine(logLine);
                if (timeStr != null) {
                    long timestamp = parseDateToTimestamp(timeStr);
                    latestTimestamp = Math.max(latestTimestamp, timestamp);
                }
            }
            return latestTimestamp > 0 ? Optional.of(latestTimestamp) : Optional.empty();
        } catch (Exception e) {
            log.error("Failed to extract latest timestamp from logs", e);
            return Optional.empty();
        }
    }

    private String extractTimeFromLogLine(String logLine) {
        int startIdx = logLine.indexOf("\"time\":\"");
        if (startIdx == -1) return null;

        int endIdx = logLine.indexOf("\"", startIdx + 8);
        return logLine.substring(startIdx + 8, endIdx);
    }

    private long parseDateToTimestamp(String dateStr) {
        try {
            return Instant.parse(dateStr).toEpochMilli();
        } catch (Exception e) {
            log.error("Failed to parse date: {}", dateStr, e);
            return 0;
        }
    }

}

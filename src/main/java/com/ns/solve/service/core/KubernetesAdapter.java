package com.ns.solve.service.core;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class KubernetesAdapter {

    @Value("${last.accessed.file.path}")
    private String lastAccessedFilePath;

    private final CoreV1Api coreApi;
    private final CustomObjectsApi customObjectsApi;


    // 특정 Pod 생성
    public V1Pod createPod(Long userId, Long problemId, String namespace, String image, Map<String, Integer> resourceLimits) throws ApiException {
        String podName = PodBuilder.getPodName(userId, problemId);

        Map<String, String> labels = new HashMap<>();
        labels.put("app", "solve");
        labels.put("userId", String.valueOf(userId));
        labels.put("problemId", String.valueOf(problemId));

        try {
            V1Pod pod = new V1Pod()
                    .metadata(new V1ObjectMeta()
                            .name(podName)
                            .namespace(namespace)
                            .labels(labels))
                    .spec(PodBuilder.buildPodSpec(podName, image, resourceLimits));
            return coreApi.createNamespacedPod(namespace, pod, null, null, null, null);
        } catch (ApiException e) {
            log.error("createPod 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
            throw e;
        }
    }

    // 특정 Pod을 삭제
    public void deletePod(String namespace, String podName) throws ApiException {
        try {
            coreApi.deleteNamespacedPod(podName, namespace, null, null, null, null, null, null);
        } catch (ApiException e) {
            log.error("deletePod 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
            throw e;
        }
    }

    // NAMESPACE 내의 전체 Pod 목록 반환
    public V1PodList getPodList(String namespace) throws ApiException {
        try {
            return coreApi.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null, null);
        } catch (ApiException e) {
            log.error("getPodList 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
            throw e;
        }
    }

    // 특정 Pod 조회 (V1Pod)
    public V1Pod getPod(String namespace, String podName) throws ApiException {
        try {
            return coreApi.readNamespacedPod(podName, namespace, null);
        } catch (ApiException e) {
            log.error("getPod 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
            throw e;
        }
    }

    // 기존 Pod 갱신
    public void replacePod(String namespace, V1Pod pod) throws ApiException {
        coreApi.replaceNamespacedPod(pod.getMetadata().getName(), namespace, pod, null, null, null, null);
    }

    // Kubernetes Service 생성
    public void createService(String namespace, V1Service service) throws ApiException {
        try {
            coreApi.createNamespacedService(namespace, service, null, null, null, null);
        } catch (ApiException e) {
            log.error("createService 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
            throw e;
        }
    }

    // Traefik IngressRoute 생성
    public void createIngressRoute(String namespace, Map<String, Object> ingressRoute) throws ApiException {
        try {
            customObjectsApi.createNamespacedCustomObject("traefik.io", "v1alpha1", namespace, "ingressroutes", ingressRoute, null, null, null);
        } catch (ApiException e) {
            log.error("createIngressRoute 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
            throw e;
        }
    }

    // labelSelector 조건에 맞는 Pod 목록을 반환
    public V1PodList getPodsByLabelSelector(String namespace, String labelSelector) {
        try {
            // labelSelector "app=solve,type=dedicated"
            return coreApi.listNamespacedPod(namespace, null, null, null, null, labelSelector, null, null, null, null, null);
        } catch (ApiException e) {
            log.error("getPodsByLabelSelector 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
            return new V1PodList();
        }
    }

    // 특정 Pod의 상태(phase)를 반환
    public Optional<String> getPodPhase(String namespace, String podName) {
        try {
            V1Pod pod = getPod(namespace, podName);
            return Optional.ofNullable(pod.getStatus().getPhase());
        } catch (Exception e) {
            log.warn("Could not get phase for pod {}: {}", podName, e.getMessage());
            return Optional.empty();
        }
    }

    // 특정 Pod의 Log를 반환
    public String getPodLogs(String namespace, String podName) {
        try {
            return coreApi.readNamespacedPodLog(podName, namespace, null, null, null, null, null, null, null, null, null);
        } catch (ApiException e) {
            log.error("Failed to get logs for pod {}: {}", podName, e.getResponseBody());
            return "Log fetch failed: " + e.getMessage();
        }
    }

    // 특정 Pod이 Ready 상태인지 확인
    public boolean isPodReady(String namespace, String podName) {
        try {
            V1Pod pod = getPod(namespace, podName);
            return pod.getStatus().getConditions().stream()
                    .anyMatch(cond -> "Ready".equals(cond.getType()) && "True".equals(cond.getStatus()));
        } catch (Exception e) {
            log.warn("Failed to check readiness of pod {}: {}", podName, e.getMessage());
            return false;
        }
    }

    // 주어진 시간동안 Pod이 Ready 상태 될때까지 대기
    public boolean waitPodToReady(String namespace, String podName, int timeoutSeconds) {
        int waited = 0;
        int interval = 2;

        while (waited < timeoutSeconds) {
            try {
                if (isPodReady(namespace, podName)) return true;
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

    // 해당 Pod에게 input 입력값 실행, command는 추가 명령어 - 기본적인 쉘 명령어 실행과 결과 반환 정도
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


    public Optional<Long> getLatestRequestTimestamp(String namespace, String podName) {
        try {
            String filePath = String.format("%s:%s.json", lastAccessedFilePath, podName);
            String commandOutput = execPodCommand(podName, namespace, List.of("cat", "/shared/" + filePath));

            if (commandOutput == null || commandOutput.isBlank()) return Optional.empty();

            long timestamp = Long.parseLong(commandOutput.trim()) * 1000;
            return Optional.of(timestamp);
        } catch (Exception e) {
            log.warn("[K8S] Failed to read access time for {}: {}", podName, e.getMessage());
            return Optional.empty();
        }
    }


    // Pod 안에서 직접 작업 처리를 위한 용도
    private String execPodCommand(String podName, String namespace, List<String> command) {
        List<String> fullCommand = new ArrayList<>();
        fullCommand.add("kubectl");
        fullCommand.add("exec");
        fullCommand.add(podName);
        fullCommand.add("--namespace=" + namespace);
        fullCommand.add("--");
        fullCommand.addAll(command);

        ProcessBuilder processBuilder = new ProcessBuilder(fullCommand);

        try {
            Process process = processBuilder.start();
            try (Scanner scanner = new Scanner(process.getInputStream())) {
                return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            }
        } catch (IOException e) {
            log.error("Error executing command on pod {}: {}", podName, e.getMessage());
            return null;
        }
    }

    public void createNamespace(String namespaceName) throws ApiException {
        V1Namespace namespace = new V1Namespace()
                .metadata(new V1ObjectMeta()
                        .name(namespaceName))
                .spec(new V1NamespaceSpec());
        try {
            coreApi.createNamespace(namespace, null, null, null, null);
        } catch (ApiException e) {
            log.error("createNamespace 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
            throw e;
        }
    }
}

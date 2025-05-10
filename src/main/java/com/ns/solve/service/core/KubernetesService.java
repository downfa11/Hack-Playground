package com.ns.solve.service.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.Exec;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class KubernetesService {
    public static final String TRAEFIK_REPLACEPATHREGEX_MIDDLEWARE_NAME = "replace-path-regex-middleware";

    private final CoreV1Api coreApi;
    private final CustomObjectsApi customObjectsApi;

    private final ObjectMapper mapper = new ObjectMapper();


    // 특정 Pod 생성
    public V1Pod createPod(Long userId, Long problemId, String namespace, String image, Map<String, Integer> resourceLimits) throws ApiException {
        String podName = PodBuilder.getPodName(userId, problemId);

        Map<String, String> labels = new HashMap<>();
        labels.put("app", podName);
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

    // 특정 Pod을 강제로 삭제
    public void forceDeletePod(String namespace, String podName) throws ApiException {
        try {
            V1DeleteOptions deleteOptions = new V1DeleteOptions()
                    .gracePeriodSeconds(0L)
                    .propagationPolicy("Foreground");

            coreApi.deleteNamespacedPod(podName, namespace, null, null, 0, false, "Foreground", deleteOptions);
        } catch (ApiException e) {
            log.error("forceDeletePod 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
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

    public V1ServiceList getServiceList(String namespace) throws ApiException {
        try {
            return coreApi.listNamespacedService(namespace, null, null, null, null, null, null, null, null, null, null);
        } catch (ApiException e) {
            log.error("getServiceList 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
            throw e;
        }
    }

    public void deleteService(String namespace, String serviceName) throws ApiException {
        try {
            coreApi.deleteNamespacedService(serviceName, namespace, null, null, null, null, null, null);
            log.info("Service 삭제 성공: {}", serviceName);
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                log.warn("Service 미존재, 건너뜀: {}", serviceName);
            } else {
                log.error("deleteService 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
                throw e;
            }
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

    public Map<String, Object> getIngressRouteByName(String namespace, String ingressRouteName) throws ApiException {
        try {
            Object obj = customObjectsApi.getNamespacedCustomObject("traefik.io", "v1alpha1", namespace, "ingressroutes", ingressRouteName);
            return (Map<String, Object>) obj;
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                log.warn("IngressRoute 미존재: {}", ingressRouteName);
                return null;
            }
            log.error("getIngressRouteByName 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
            throw e;
        }
    }


    public Map<String, Object> getIngressRouteList(String namespace) throws ApiException {
        try {
            Object obj = customObjectsApi.listNamespacedCustomObject("traefik.io", "v1alpha1", namespace, "ingressroutes", null, null, null, null, null, null, null, null,30,false);
            return (Map<String, Object>) obj;
        } catch (ApiException e) {
            log.error("getIngressRouteList 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
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

    public void deleteIngressRoute(String namespace, String ingressRouteName) throws ApiException {
        try {
            customObjectsApi.deleteNamespacedCustomObject("traefik.io", "v1alpha1", namespace, "ingressroutes", ingressRouteName, null, null, null, null, null);
            log.info("IngressRoute 삭제 성공: {}", ingressRouteName);
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                log.warn("IngressRoute 미존재, 건너뜀: {}", ingressRouteName);
            } else {
                log.error("deleteIngressRoute 실패: code={}, body={}", e.getCode(), e.getResponseBody(), e);
                throw e;
            }
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
    public String getPodLogs(String namespace, String podName, String containerName) {
        try {
            return coreApi.readNamespacedPodLog(podName, namespace, containerName, null, null, null, null, null, null, null, null);
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
            String filePath = "/tmp/last_connections.json";
            String attache = "attache-sidecar";
            String commandOutput = execPodCommand(podName, namespace, attache, List.of("cat", filePath));
            log.info("getLatestRequestTimestamp {} result : {}" ,attache, commandOutput);

            if (commandOutput == null || commandOutput.isBlank()) return Optional.empty();
            List<Map<String, Object>> records = mapper.readValue(commandOutput, new TypeReference<>() {});

            Optional<Long> latest = records.stream()
                    .map(entry -> entry.get("timestamp"))
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(ts -> Instant.parse(ts).toEpochMilli())
                    .filter(Objects::nonNull)
                    .max(Long::compareTo);

            return latest;
        } catch (Exception e) {
            log.warn("[K8S] Failed to read access time for {}: {}", podName, e.getMessage());
            return Optional.empty();
        }
    }



    // Pod 안에서 직접 작업 처리를 위한 용도
    private String execPodCommand(String podName, String namespace, String container, List<String> command) {
        try {
            Exec exec = new Exec();
            String[] cmd = command.toArray(new String[0]);
            Process process = exec.exec(namespace, podName, cmd, container, false, false);
            String result = new String(process.getInputStream().readAllBytes());

            return result;
        } catch (Exception e) {
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

    public void deleteAllResourcesByLabel(String namespace, String labelSelector) throws ApiException {
        deletePodsByLabel(namespace, labelSelector);
        deleteServicesByLabel(namespace, labelSelector);
        deleteIngressRoutesByLabel(namespace, labelSelector);
        deleteMiddlewaresBySelector(namespace, labelSelector);
    }

    private void deletePodsByLabel(String namespace, String labelSelector) throws ApiException {
        V1PodList pods = coreApi.listNamespacedPod(namespace, null, null, null, null, labelSelector, null, null, null, null, null);
        for (V1Pod pod : pods.getItems()) {
            String name = pod.getMetadata().getName();
            coreApi.deleteNamespacedPod(name, namespace, null, null, null, null, null, null);
            log.info("Pod 삭제됨: {}", name);
        }
    }

    private void deleteServicesByLabel(String namespace, String labelSelector) throws ApiException {
        V1ServiceList services = coreApi.listNamespacedService(namespace, null, null, null, null, labelSelector, null, null, null, null, null);
        for (V1Service service : services.getItems()) {
            String name = service.getMetadata().getName();
            try {
                coreApi.deleteNamespacedService(name, namespace, null, null, null, null, null, null);
                log.info("Service 삭제됨: {}", name);
            } catch (ApiException e) {
                if (e.getCode() != 404) throw e;
                log.warn("Service 미존재 건너뜀: {}", name);
            }
        }
    }

    private void deleteIngressRoutesByLabel(String namespace, String labelSelector) throws ApiException {
        Object obj = customObjectsApi.listNamespacedCustomObject("traefik.io", "v1alpha1", namespace, "ingressroutes", null, null, null, null, labelSelector, 30,null ,null,null,false);

        Map<String, Object> map = (Map<String, Object>) obj;
        List<Map<String, Object>> items = (List<Map<String, Object>>) map.get("items");

        for (Map<String, Object> item : items) {
            Map<String, Object> metadata = (Map<String, Object>) item.get("metadata");
            String name = (String) metadata.get("name");

            try {
                customObjectsApi.deleteNamespacedCustomObject(
                        "traefik.io", "v1alpha1", namespace, "ingressroutes",
                        name, null, null, null, null, null
                );
                log.info("IngressRoute 삭제됨: {}", name);
            } catch (ApiException e) {
                if (e.getCode() != 404) throw e;
                log.warn("IngressRoute 미존재 건너뜀: {}", name);
            }
        }
    }

    public List<Map<String, Object>> listAllMiddlewares(String namespace, String labelSelector) throws ApiException {
        Map<String, Object> response = (Map<String, Object>) customObjectsApi.listNamespacedCustomObject("traefik.io", "v1alpha1", namespace, "middlewares", null, null, null, null, labelSelector, null, null, null, null, null);
        return (List<Map<String, Object>>) response.getOrDefault("items", List.of());
    }

    public void deleteMiddlewaresBySelector(String namespace, String labelSelector) throws ApiException {
        List<Map<String, Object>> middlewares = listAllMiddlewares(namespace, labelSelector);

        for (Map<String, Object> middleware : middlewares) {
            Map<String, Object> metadata = (Map<String, Object>) middleware.get("metadata");
            String name = (String) metadata.get("name");

            V1DeleteOptions deleteOptions = new V1DeleteOptions();
            deleteOptions.setPropagationPolicy("Foreground");
            customObjectsApi.deleteNamespacedCustomObject("traefik.io", "v1alpha1", namespace, "middlewares", name, null, null, "Foreground", null, deleteOptions);

            log.info("Traefik Middleware 삭제됨: {}", name);
        }

        log.info("총 {}개의 미들웨어 삭제됨 - selector: {}", middlewares.size(), labelSelector);
    }


    // StripPrefix 생성
    public void createStripPrefixMiddleware(String namespace, Long userId, Long problemId, String uuid) throws ApiException {
        Map<String, Object> middleware = PodBuilder.buildStripPrefixMiddleware(userId, problemId, uuid);
        customObjectsApi.createNamespacedCustomObject(
                "traefik.io", "v1alpha1", namespace, "middlewares", middleware, null, null, null
        );
    }

    // ReplacePathRegex 생성
    public void createReplacePathRegexMiddleware(String namespace, String name, String regex, String replacement) throws ApiException {
        Map<String, Object> middleware = PodBuilder.buildReplacePathRegexMiddleware(TRAEFIK_REPLACEPATHREGEX_MIDDLEWARE_NAME);
        customObjectsApi.createNamespacedCustomObject(
                "traefik.io", "v1alpha1", namespace, "middlewares", middleware, null, null, null
        );
    }

    public List<V1Service> getServicesByLabelSelector(String namespace, String labelSelector) throws ApiException {
        return coreApi.listNamespacedService(namespace, null, null, null,null, labelSelector,null, null, null, null, null)
                .getItems();
    }


    public List<V1Service> getServicesByLabelSelectorInAllNamespaces(String labelSelector) throws ApiException {
        return coreApi.listServiceForAllNamespaces(null, null, null, labelSelector,null, null, null, null, null, null)
                .getItems();
    }

}

package com.ns.solve.service.core;

import com.ns.solve.domain.vo.WargameKind;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.*;

import java.util.*;

public class PodBuilder {

    public static V1PodSpec buildPodSpec(Long problemId, Long userId, Integer targetPort, Integer nodePort, WargameKind kind, String image, Map<String, Integer> resourceLimits) {
        String podName = PodBuilder.getPodName(userId, problemId);
        List<V1Container> containers = new ArrayList<>(List.of(buildContainer(podName, image, resourceLimits), buildSidecarContainer(problemId, userId, targetPort)));

        if(kind.equals(WargameKind.WEBHACKING)) {  // 웹문제면 reverse-proxy-container 추가
            containers.add(buildReverseProxyContainer(problemId, userId, kind, targetPort, nodePort));
        }

        return new V1PodSpec()
                .containers(containers)
                .restartPolicy("OnFailure") // 비정상적인 종료시 재시작
                // .runtimeClassName("gvisor")
                .securityContext(new V1PodSecurityContext()
                        // .seccompProfile(new V1SeccompProfile().type("RuntimeDefault"))
                        .seccompProfile(new V1SeccompProfile().type("Unconfined"))
                        .runAsNonRoot(false)
                        .runAsUser(1001L)
                        .runAsGroup(1001L)
                        .fsGroup(1001L)
                        .sysctls(Arrays.asList(
                                new V1Sysctl().name("net.ipv4.tcp_keepalive_time").value("60"),
                                new V1Sysctl().name("net.ipv4.tcp_keepalive_intvl").value("10"),
                                new V1Sysctl().name("net.ipv4.tcp_keepalive_probes").value("4")
                        )))
                .automountServiceAccountToken(false)
                .hostNetwork(false);

    }

    private static V1Container buildContainer(String containerName, String image, Map<String, Integer> resourceLimits) {
        return new V1Container()
                .name(containerName + "-container")
                .image(image)
                .securityContext(new V1SecurityContext()
                        .allowPrivilegeEscalation(false)) // readOnlyRootFilesystem(true), runAsNonRoot(true)
                .resources(createResourceRequirements(resourceLimits));
    }

    public static V1ResourceRequirements createResourceRequirements(Map<String, Integer> resourceLimits) {
        // 좀 더 섬세한 조절이 가능하도록 할지는 고민
        if (resourceLimits == null) {
            resourceLimits = new HashMap<>();
            resourceLimits.put("cpu", 200);
            resourceLimits.put("memory", 256);
        }

        Integer cpuLimit = resourceLimits.getOrDefault("cpu", 200);
        Integer memoryLimit = resourceLimits.getOrDefault("memory", 256);

        Integer cpuRequest = Math.max(cpuLimit / 2, 50);
        Integer memoryRequest = Math.max(memoryLimit / 2, 64);

        return new V1ResourceRequirements()
                .limits(Map.of("cpu", new Quantity(cpuLimit + "m"), "memory", new Quantity(memoryLimit + "Mi")))
                .requests(Map.of("cpu", new Quantity(cpuRequest + "m"), "memory", new Quantity(memoryRequest + "Mi")));
    }

    public static V1ResourceRequirements createSideCarResourceRequirements() {
        return new V1ResourceRequirements()
                .limits(Map.of("cpu", new Quantity("200m"), "memory", new Quantity("256Mi")))
                .requests(Map.of("cpu", new Quantity("50m"), "memory", new Quantity("64Mi")));
    }

    public static String sanitizeName(String name) {
        // kubernetes 네이밍 규칙으로 변환
        return name.toLowerCase()
                .replaceAll("[^a-z0-9.-]", "")
                .replaceAll("^[^a-z0-9]+", "")
                .replaceAll("[^a-z0-9]+$", "");
    }

    public static String getPodName(Long userId, Long problemId) {
        return sanitizeName("Problem" + problemId + "-" + userId);
    }

    public static String getPodName(Long problemId) {
        return sanitizeName("Problem" + problemId);
    }

    // webhacking 문제는 ClusterIP Service + IngressRoute
    // 포렌식 등의 쉡 접속 문제는 NodePort Service + TCP
    public static V1Service buildService(Long userId, Long problemId, WargameKind kind, Integer port) {
        V1Service service = new V1Service();
        V1ObjectMeta metadata = new V1ObjectMeta();
        String podName = getPodName(userId, problemId);
        metadata.setName(podName);

        Map<String, String> labels = new HashMap<>();
        labels.put("app", podName);
        labels.put("userId", String.valueOf(userId));
        labels.put("problemId", String.valueOf(problemId));
        labels.put("kind", String.valueOf(kind));

        metadata.setLabels(labels);
        service.setMetadata(metadata);

        V1ServiceSpec spec = new V1ServiceSpec();
        spec.setSelector(Map.of("app", podName)); // 해당 Pod를 찾는다.
        spec.setPorts(List.of(new V1ServicePort()
                .port(port)
                .targetPort(new IntOrString(port))));

        // String type = kind.equals(WargameKind.WEBHACKING) ? "ClusterIP" : "NodePort";
        spec.setType("NodePort");
        service.setSpec(spec);
        return service;
    }

    public static Map<String, Object> buildReplacePathRegexMiddleware(String middlewareName) {
        Map<String, String> labels = new HashMap<>();
        labels.put("app", middlewareName);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", middlewareName);
        metadata.put("labels", labels);
        labels.put("kind", "WEBHACKING");

        Map<String, Object> middleware = new HashMap<>();
        middleware.put("apiVersion", "traefik.io/v1alpha1");
        middleware.put("kind", "Middleware");
        middleware.put("metadata", metadata);
        middleware.put("spec", Map.of(
                "replacePathRegex", Map.of(
                        "regex", "^/problems/\\d+/[a-f0-9\\-]+(?:/(.*))?",
                        "replacement", "/$1")
        ));

        return middleware;
    }

    public static Map<String, Object> buildStripPrefixMiddleware(Long userId, Long problemId, String uuid) {
        String podName = getPodName(userId, problemId);

        Map<String, String> labels = new HashMap<>();
        labels.put("app", podName);
        labels.put("userId", String.valueOf(userId));
        labels.put("problemId", String.valueOf(problemId));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name",podName);
        metadata.put("labels", labels);

        Map<String, Object> middleware = new HashMap<>();
        middleware.put("apiVersion", "traefik.io/v1alpha1");
        middleware.put("kind", "Middleware");
        middleware.put("metadata", metadata);

        String pathPrefix = String.format("/problems/%d/%s", problemId, uuid);
        middleware.put("spec", Map.of(
                "stripPrefix", Map.of(
                        "prefixes", List.of(pathPrefix),
                        "forceSlash", true
                )
        ));

        return middleware;
    }

    public static Map<String, Object> buildRewritePathRegexMiddleware(Long userId, Long problemId, String uuid) {
        String podName = getPodName(userId, problemId);

        Map<String, String> labels = new HashMap<>();
        labels.put("app", podName);
        labels.put("userId", String.valueOf(userId));
        labels.put("problemId", String.valueOf(problemId));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", podName);
        metadata.put("labels", labels);

        // /problems/{problemId}/{uuid}/(.*) → /$1
        String regex = String.format("^/problems/%d/%s/(.*)", problemId, uuid);
        String replacement = "/$1";

        Map<String, Object> spec = Map.of("replacePathRegex", Map.of("regex", regex, "replacement", replacement));

        Map<String, Object> middleware = new HashMap<>();
        middleware.put("apiVersion", "traefik.io/v1alpha1");
        middleware.put("kind", "Middleware");
        middleware.put("metadata", metadata);
        middleware.put("spec", spec);

        return middleware;
    }


    public static Map<String, Object> buildIngressRoute(Long userId, Long problemId, Integer port, String namespace, String uuid) {
        String podName = getPodName(userId, problemId);

        Map<String, String> labels = new HashMap<>();
        labels.put("app", podName);
        labels.put("userId", String.valueOf(userId));
        labels.put("problemId", String.valueOf(problemId));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", podName);
        metadata.put("labels", labels);

        Map<String, Object> route = new HashMap<>();

        String pathPrefix = String.format("/problems/%d/%s", problemId, uuid);
        route.put("match",  String.format("PathPrefix(`%s`)", pathPrefix));
        route.put("kind", "Rule");

        route.put("middlewares", List.of(Map.of("name", podName, "namespace", namespace)));
        // stripPrefix, RewritePathRegex, route.put("middlewares", List.of(Map.of("name", "replace-path-regex-middleware", "namespace", namespace)));
        route.put("services", List.of(Map.of("name", podName, "port", port)));

        Map<String, Object> spec = new HashMap<>();
        spec.put("entryPoints", List.of("web"));
        spec.put("routes", List.of(route));

        Map<String, Object> ingressRoute = new HashMap<>();
        ingressRoute.put("apiVersion", "traefik.io/v1alpha1");
        ingressRoute.put("kind", "IngressRoute");
        ingressRoute.put("metadata", metadata);
        ingressRoute.put("spec", spec);

        return ingressRoute;
    }

    // 현재 io.kubernetes.client.openapi.models.V1Container에는 lifecycle.type 없음 (kubernetes native sidecar)
    private static V1Container buildSidecarContainer(Long problemId, Long userId, Integer targetPort) {
        V1EnvVar problemIdEnv = new V1EnvVar().name("PROBLEM_ID").value(String.valueOf(problemId));
        V1EnvVar userIdEnv = new V1EnvVar().name("USER_ID").value(String.valueOf(userId));
        V1EnvVar filePath = new V1EnvVar().name("FILE_PATH").value("/tmp/last_connections.json");
        V1EnvVar port = new V1EnvVar().name("PORT").value("18888");
        V1EnvVar targetPortEnv = new V1EnvVar().name("TARGET_PORT").value(String.valueOf(targetPort));


        List<V1EnvVar> envVars = List.of(problemIdEnv, userIdEnv, filePath, port, targetPortEnv);

        return new V1Container()
                .name("attache-sidecar")
                .image("downfa11/attache:latest")
                .ports(List.of(new V1ContainerPort().containerPort(18888)))
                .env(envVars)
                .resources(createSideCarResourceRequirements())
                .securityContext(new V1SecurityContext()
                        .allowPrivilegeEscalation(true)
                        .runAsUser(0L)
                        .capabilities(new V1Capabilities().addAddItem("NET_ADMIN")
                                .addAddItem("NET_RAW")
                                .addAddItem("NET_BPF"))
                );
    }

    private static V1Container buildReverseProxyContainer(Long problemId, Long userId, WargameKind kind, Integer targetPort, Integer nodePort) {
        V1EnvVar problemIdEnv = new V1EnvVar().name("PROBLEM_ID").value(String.valueOf(problemId));
        V1EnvVar userIdEnv = new V1EnvVar().name("USER_ID").value(String.valueOf(userId));
        V1EnvVar kindEnv = new V1EnvVar().name("PROBLEM_KIND").value(String.valueOf(kind));
        V1EnvVar httpPortEnv = new V1EnvVar().name("HTTP_PORT").value(String.valueOf(targetPort));
        V1EnvVar nodePortEnv = new V1EnvVar().name("NODE_PORT").value(String.valueOf(nodePort));

        List<V1EnvVar> envVars = List.of(problemIdEnv, userIdEnv, kindEnv, httpPortEnv, nodePortEnv);

        return new V1Container()
                .name("detache-sidecar")
                .image("downfa11/detache:latest")
                .ports(List.of(new V1ContainerPort().containerPort(18889)))
                .env(envVars)
                .resources(createSideCarResourceRequirements())
                .securityContext(new V1SecurityContext()
                        .allowPrivilegeEscalation(false)
                        .runAsNonRoot(true)
                );
    }

}

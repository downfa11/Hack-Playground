package com.ns.solve.service.core;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PodBuilder {

    public static V1PodSpec buildPodSpec(String podName, String image, Map<String, Integer> resourceLimits) {
        return new V1PodSpec()
                .containers(List.of(buildContainer(podName, image, resourceLimits), buildSidecarContainer()))
                .restartPolicy("OnFailure") // 비정상적인 종료시 재시작
                // .runtimeClassName("gvisor")
                .securityContext(new V1PodSecurityContext()
                        // .seccompProfile(new V1SeccompProfile().type("RuntimeDefault"))
                        .seccompProfile(new V1SeccompProfile().type("Unconfined"))
                        .runAsNonRoot(false))
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

    public static V1Service buildService(Long userId, Long problemId, Integer port) {
        V1Service service = new V1Service();
        V1ObjectMeta metadata = new V1ObjectMeta();
        String podName = getPodName(userId, problemId);
        metadata.setName(podName);

        Map<String, String> labels = new HashMap<>();
        labels.put("app", podName);
        labels.put("userId", String.valueOf(userId));
        labels.put("problemId", String.valueOf(problemId));

        metadata.setLabels(labels);
        service.setMetadata(metadata);

        V1ServiceSpec spec = new V1ServiceSpec();
        spec.setSelector(Map.of("app", podName)); // 해당 Pod를 찾는다.
        spec.setPorts(List.of(new V1ServicePort()
                .port(port)
                .targetPort(new IntOrString(port))));
        spec.setType("ClusterIP");

        service.setSpec(spec);
        return service;
    }

    public static Map<String, Object> buildReplacePathRegexMiddleware(String middlewareName) {
        Map<String, String> labels = new HashMap<>();
        labels.put("app", middlewareName);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", middlewareName);
        metadata.put("labels", labels);

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
        metadata.put("name", podName+"-"+uuid);
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
    private static V1Container buildSidecarContainer() {
    //  V1EnvVar problemPortEnv = new V1EnvVar().name("PROBLEM_APP_PORT").value(String.valueOf(problemPort));
    //  V1EnvVar problemIdEnv = new V1EnvVar().name("PROBLEM_ID").value(String.valueOf(problemId));
    //  V1EnvVar userIdEnv = new V1EnvVar().name("USER_ID").value(String.valueOf(userId));
    //  V1EnvVar uuidEnv = new V1EnvVar().name("UUID").value(uuid);
        V1EnvVar filePath = new V1EnvVar().name("FILE_PATH").value("/tmp/last_connections.json");
        V1EnvVar port = new V1EnvVar().name("PORT").value(":1880");

        List<V1EnvVar> envVars = List.of(filePath, port);

        return new V1Container()
                .name("attache-sidecar")
                .image("downfa11/attache:latest")
                .ports(List.of(new V1ContainerPort().containerPort(8888)))
                .env(envVars)
                .resources(createSideCarResourceRequirements())
                .securityContext(new V1SecurityContext()
                        .allowPrivilegeEscalation(true)
                        .runAsUser(0L)
                        .capabilities(new V1Capabilities().addAddItem("NET_ADMIN")
                                .addAddItem("NET_RAW"))
                );
    }

}

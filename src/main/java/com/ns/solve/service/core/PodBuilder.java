package com.ns.solve.service.core;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PodBuilder {

    private static String lastAccessedFilePath = "/last-accessed";

    public static V1PodSpec buildPodSpec(String name, String image, Map<String, Integer> resourceLimits) {

        return new V1PodSpec()
                .containers(List.of(buildContainer(name, image, resourceLimits), buildSidecarContainer(name)))
                .restartPolicy("OnFailure") // 비정상적인 종료시 재시작
                // .runtimeClassName("gvisor")
                .securityContext(new V1PodSecurityContext()
                        .seccompProfile(new V1SeccompProfile().type("RuntimeDefault"))) //.runAsNonRoot(true)
                .automountServiceAccountToken(false)
                .volumes(List.of(sharedVolume())); // sidecar로 받기 위해서 공유 볼륨 설정

    }

    private static V1Container buildContainer(String podName, String image, Map<String, Integer> resourceLimits) {
        return new V1Container()
                .name(podName + "-container")
                .image(image)
                .securityContext(new V1SecurityContext()
                        .allowPrivilegeEscalation(false)) // readOnlyRootFilesystem(true), runAsNonRoot(true)
                .resources(createResourceRequirements(resourceLimits));
    }

    public static V1ResourceRequirements createResourceRequirements(Map<String, Integer> resourceLimits) {
        // 좀 더 섬세한 조절이 가능하도록 할지는 고민
        if (resourceLimits == null) {
            resourceLimits = new HashMap<>();
            resourceLimits.put("cpu", 100);
            resourceLimits.put("memory", 256);
        }

        Integer cpuLimit = resourceLimits.getOrDefault("cpu", 100);
        Integer memoryLimit = resourceLimits.getOrDefault("memory", 256);

        Integer cpuRequest = Math.max(cpuLimit / 2, 50);
        Integer memoryRequest = Math.max(memoryLimit / 2, 128);

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
        return sanitizeName(userId + "-" + problemId);
    }

    public static String getPodName(Long problemId) {
        return sanitizeName(String.valueOf(problemId));
    }

    public static V1Service buildService(String podName, Integer port) {
        V1Service service = new V1Service();

        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(podName);
        metadata.setLabels(Map.of("app", "solve", "podName", podName));
        service.setMetadata(metadata);

        V1ServiceSpec spec = new V1ServiceSpec();
        spec.setSelector(Map.of("app", podName));
        spec.setPorts(List.of(new V1ServicePort()
                .port(port)
                .targetPort(new IntOrString(port))));
        spec.setType("ClusterIP");

        service.setSpec(spec);
        return service;
    }

    public static Map<String, Object> buildIngressRoute(String podName, Integer port) {
        return Map.of(
                "apiVersion", "traefik.io/v1alpha1", "kind", "IngressRoute",
                "metadata", Map.of("name", podName),
                "spec", Map.of("entryPoints", List.of("websecure"),
                        "routes", List.of(
                                Map.of("match", "PathPrefix(`/" + podName + "`)", "kind", "Rule",
                                        "services", List.of(Map.of("name", podName, "port", port))
                                )
                        )
                )
        );
    }

    // 현재 io.kubernetes.client.openapi.models.V1Container에는 lifecycle.type 없음 (kubernetes native sidecar)
    private static V1Container buildSidecarContainer(String getPodName) {
        return new V1Container()
                .name("attache-sidecar")
                .image("downfa11/attache:latest")
                .ports(List.of(new V1ContainerPort().containerPort(8080)))
                .env(List.of(envVar("filePath", String.format("/shared/%s:%s.json", lastAccessedFilePath, getPodName))))
                .volumeMounts(List.of(sharedVolumeMount()))
                .resources(createSideCarResourceRequirements())
                .securityContext(new V1SecurityContext()
                        .allowPrivilegeEscalation(false)
                        .runAsUser(1000L)
                        .capabilities(new V1Capabilities().addAddItem("NET_ADMIN"))
                );
    }

    private static V1Volume sharedVolume() {
        return new V1Volume()
                .name("shared")
                .emptyDir(new V1EmptyDirVolumeSource());
    }

    private static V1VolumeMount sharedVolumeMount() {
        return new V1VolumeMount()
                .name("shared")
                .mountPath("/shared");
    }

    private static V1EnvVar envVar(String name, String value) {
        return new V1EnvVar().name(name).value(value);
    }

}

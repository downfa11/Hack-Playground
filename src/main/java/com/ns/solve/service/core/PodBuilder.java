package com.ns.solve.service.core;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.*;

import java.util.List;
import java.util.Map;

public class PodBuilder {
    public static V1PodSpec buildPodSpec(String name, String image, List<String> command, List<String> args) {
        return new V1PodSpec()
                .containers(List.of(buildContainer(name, image, command, args)))
                .restartPolicy("Never")
                // .runtimeClassName("gvisor")
                .securityContext(new V1PodSecurityContext()
                        //.runAsNonRoot(true)
                        .seccompProfile(new V1SeccompProfile().type("RuntimeDefault")))
                .automountServiceAccountToken(false);
    }
    private static V1Container buildContainer(String name, String image, List<String> command, List<String> args) {
        return new V1Container()
                .name(name + "-container")
                .image(image)
                .command(command)
                .args(args)
                .securityContext(new V1SecurityContext()
                        //.readOnlyRootFilesystem(true)
                        .allowPrivilegeEscalation(false)
                        //.runAsNonRoot(true)
                        )
                .resources(createResourceRequirements());
    }
    public static V1ResourceRequirements createResourceRequirements() {
        return new V1ResourceRequirements()
                .limits(Map.of(
                        "cpu", new Quantity("500m"),
                        "memory", new Quantity("512Mi")
                ))
                .requests(Map.of(
                        "cpu", new Quantity("250m"),
                        "memory", new Quantity("256Mi")
                ));
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

    public static V1Service buildService(String podName) {
        V1Service service = new V1Service();

        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(podName);
        metadata.setLabels(Map.of("app", "solve", "podName", podName));
        service.setMetadata(metadata);

        V1ServiceSpec spec = new V1ServiceSpec();
        spec.setSelector(Map.of("app", podName));
        spec.setPorts(List.of(new V1ServicePort()
                .port(80)
                .targetPort(new IntOrString(80))));
        spec.setType("ClusterIP");

        service.setSpec(spec);
        return service;
    }

    public static Map<String, Object> buildIngressRoute(String podName) {
        return Map.of(
                "apiVersion", "traefik.io/v1alpha1",
                "kind", "IngressRoute",
                "metadata", Map.of("name", podName),
                "spec", Map.of(
                        "entryPoints", List.of("web"),
                        "routes", List.of(
                                Map.of(
                                        "match", "PathPrefix(`/" + podName + "`)",
                                        "kind", "Rule",
                                        "services", List.of(Map.of(
                                                "name", podName,
                                                "port", 80
                                        ))))));
    }


}

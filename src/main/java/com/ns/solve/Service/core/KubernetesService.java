package com.ns.solve.service.core;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1HTTPGetAction;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1Probe;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class KubernetesService {
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private static final String NAMESPACE = "default";
    private static final String WORKER_POD_NAME = "worker-pod";
    private final CoreV1Api api;

    public KubernetesService() throws IOException {
        this.api = new CoreV1Api(Config.defaultClient());
        executorService.scheduleAtFixedRate(this::ensureWorkerPodExists, 0, 30, TimeUnit.SECONDS);
    }

    // Worker Pod Pool 생성 - HPA 가능
    private void ensureWorkerPodExists() {
        try {
            api.readNamespacedPod(WORKER_POD_NAME, NAMESPACE, null);
            System.out.println("Worker Pod already exists.");
        } catch (ApiException e) {
            System.out.println("Worker Pod does not exist, creating...");
            createWorkerPod();
        }
    }

    private void createWorkerPod() {
        V1Pod pod = new V1Pod()
                .metadata(new V1ObjectMeta().name(WORKER_POD_NAME))
                .spec(new V1PodSpec()
                        .containers(Collections.singletonList(createContainer("worker-container", "your-image"))));

        try {
            api.createNamespacedPod(NAMESPACE, pod, null, null, null, null);
            System.out.println("Worker Pod created: " + WORKER_POD_NAME);
        } catch (ApiException e) {
            System.out.println("Error creating Worker Pod: " + e.getResponseBody());
        }
    }


    public String createPod(String podName, String image) {
        V1Pod pod = new V1Pod()
                .metadata(new V1ObjectMeta().name(podName))
                .spec(new V1PodSpec()
                        .containers(Collections.singletonList(
                                new V1Container()
                                        .name("test-container")
                                        .image(image)
                                        .command(Collections.singletonList("tail"))
                                        .args(Arrays.asList("-f", "/dev/null"))
                        ))
                );


        try {
            V1Pod createdPod = api.createNamespacedPod("default", pod, null, null, null, null);
            return "Pod created: " + createdPod.getMetadata().getName();
        } catch (ApiException e) {
            System.out.println("ApiException ResponseBody: " + e.getResponseBody());
            System.out.println("HTTP Status Code: " + e.getCode());
            System.out.println("Response Headers: " + e.getResponseHeaders());
            return "Pod failed: " + e;
        }
    }

    private V1Container createContainer(String containerName, String image) {
        return new V1Container()
                .name(containerName)
                .image(image)
                .command(Collections.singletonList("tail"))
                .args(Arrays.asList("-f", "/dev/null"))
                .resources(createResourceRequirements())
                .livenessProbe(createLivenessProbe("live",8080))
                .readinessProbe(createReadinessProbe("readiness",8080));
    }

    private V1ResourceRequirements createResourceRequirements() {
        return new V1ResourceRequirements()
                .limits(Collections.singletonMap("cpu", new Quantity("500m")))
                .limits(Collections.singletonMap("memory", new Quantity("512Mi")))
                .requests(Collections.singletonMap("cpu", new Quantity("250m")))
                .requests(Collections.singletonMap("memory", new Quantity("256Mi")));
    }

    private V1Probe createLivenessProbe(String path, Integer port) {
        return new V1Probe()
                .httpGet(new V1HTTPGetAction()
                        .path(path)
                        .port(new IntOrString(port)))
                .initialDelaySeconds(10)
                .periodSeconds(5)
                .timeoutSeconds(1)
                .failureThreshold(3);
    }

    private V1Probe createReadinessProbe(String path, Integer port) {
        return new V1Probe()
                .httpGet(new V1HTTPGetAction()
                        .path(path)
                        .port(new IntOrString(port)))
                .initialDelaySeconds(5)
                .periodSeconds(5)
                .timeoutSeconds(1)
                .failureThreshold(3);
    }

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
            System.out.println("Error executing command on pod: " + podName + " " + e.getMessage() +" "+ e);
            return "Error executing command: " + e.getMessage();
        }
    }

    public String createAndExecutePod(String podName, String image, String... command) {
        String createResult = createPod(podName, image);
        System.out.println("createAndExecutePod: " + createResult);

        String result = null;

        try {
            if (waitPodToReady(podName, 60))
                result = executeCommand(podName, command);
            else System.out.println("Pod is not in a running state after waiting.");
        } catch (Exception e){
            System.out.println("createAndExecutePod error: "+ e);
        }

        return result;
    }

    public boolean waitPodToReady(String podName, int timeout) throws ApiException, InterruptedException {
        int curDelay = 0;

        while (curDelay < timeout) {
            V1Pod pod = api.readNamespacedPod(podName, "default", null);

            String status = pod.getStatus().getPhase();
            if ("Running".equals(status)) return true;

            else System.out.println("Pod Status: "+ status);

            Thread.sleep(1000);
            curDelay++;
        }
        return false;
    }

}

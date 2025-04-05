package com.ns.solve.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesConfig {


    @Bean
    public ApiClient apiClient() throws IOException {
        ApiClient client =  Config.defaultClient(); // ~/.kube/config
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
        return client;
    }

    @Bean
    public CoreV1Api coreV1Api(ApiClient apiClient) {
        return new CoreV1Api(apiClient);
    }

    @Bean
    public String getPods(CoreV1Api coreV1Api) throws ApiException {
        V1PodList podList = coreV1Api.listNamespacedPod("default", null, null, null, null, null, null, null, null,null, null);
        System.out.println("Pods in the 'default' namespace:");
        podList.getItems().forEach(pod -> System.out.println(pod.getMetadata().getName()));
        return "Pods listed successfully";
    }
}
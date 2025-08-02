package com.ns.solve.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class KubernetesConfig {
    @Bean
    public ApiClient apiClient() throws IOException {
        ApiClient client;
        try {
            client = Config.fromCluster();
            client.setDebugging(true);

            io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
            log.info("Kubernetes API Connection successed ✅");

        } catch (Exception e) {
            log.error("Kubernetes API Connection Failed ❌", e);
            throw new IOException("Kubernetes API Connection Failed ❌: " + e.getMessage(), e);
        }

        return client;
    }

    @Bean
    public CoreV1Api coreV1Api(ApiClient apiClient) {
        return new CoreV1Api(apiClient);
    }

    @Bean
    public CustomObjectsApi customObjectsApi(ApiClient apiClient){
        return new CustomObjectsApi(apiClient);
    }

}
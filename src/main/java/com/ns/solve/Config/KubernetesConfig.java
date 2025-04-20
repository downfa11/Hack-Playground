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

    @Value("${k8s.api.server.url}")
    private String apiUrl;

    @Value("${k8s.token}")
    private String k8sToken;

    @Value("${k8s.ca.path}")
    private String caPath;


    @Bean
    public ApiClient apiClient() throws IOException {
        // 인증서와 토큰 설정
        ApiClient client = new ApiClient();

        client.setBasePath(apiUrl);
        client.setApiKey("Bearer "+ k8sToken);
        client.setDebugging(true);

        // SSL 인증서 설정
        try (InputStream caCertInputStream = new FileInputStream(caPath)) {
            client.setSslCaCert(caCertInputStream);
        }

        // ApiClient client =  Config.defaultClient(); // ~/.kube/config
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);

        try {
            CoreV1Api testApi = new CoreV1Api(client);
            testApi.listNamespace(null, null, null, null, null, null, null, null, null, null);  // 헬스 체크
            log.info("Kubernetes API Connection successed ✅");
        } catch (Exception e) {
            throw new IOException("Kubernetes API Connection Failed ❌", e);
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
package com.ns.solve.service.core;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GCScheduler {

    private static final long TTL_SECONDS = 1800; // 30min
    private static final String WARGAME_NAMESPACE = "wargame";
    private final KubernetesService kubernetesService;


    @Scheduled(fixedDelay = 10 * 60_000)
    public void cleanExpiredPods() {
        log.info("[GC] Expired pod check start");

        try {
            V1PodList podList = kubernetesService.getPodList(WARGAME_NAMESPACE);

            for (V1Pod pod : podList.getItems()) {
                String podName = pod.getMetadata().getName();
                Map<String, String> label = pod.getMetadata().getLabels();
                String userId = label.get("userId");
                String problemId = label.get("problemId");
                String labelSelector = String.format("userId=%s,problemId=%s", userId, problemId);

                try {
                    Optional<Long> lastRequestTimestampOpt = kubernetesService.getLatestRequestTimestamp(WARGAME_NAMESPACE, podName);
                    if (lastRequestTimestampOpt.isEmpty()) {
                        log.warn("[GC] Pod {} has no valid access time in logs. label: {}", podName, labelSelector);
                        kubernetesService.deleteAllResourcesByLabel(WARGAME_NAMESPACE, labelSelector);
                        continue;
                    }

                    long lastRequestTimestamp = lastRequestTimestampOpt.get();
                    long elapsed = Duration.between(Instant.ofEpochMilli(lastRequestTimestamp), Instant.now()).getSeconds();

                    if (elapsed > TTL_SECONDS) {
                        log.info("[GC] Deleting pod: {} (elapsed={}s > ttl={}s, label={})", podName, elapsed, TTL_SECONDS, labelSelector);
                        kubernetesService.deleteAllResourcesByLabel(WARGAME_NAMESPACE, labelSelector);
                    } else {
                        log.info("[GC] Skipping pod: {} (elapsed={}s <= ttl={}s)", podName, elapsed, TTL_SECONDS);
                    }

                } catch (Exception e) {
                    log.warn("[GC] Skip pod {} due to error: {}", podName, e.getMessage());
                }
            }

        } catch (ApiException e) {
            log.error("[GC] Failed to list pods: {}", e.getResponseBody());
        }
    }
}

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
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GCScheduler {
    private static final long TTL_SECONDS = 3600; // 1hour

    private final KubernetesAdapter kubernetesAdapter;


    @Scheduled(fixedDelay = 10 * 60_000)
    public void cleanExpiredPods() {
        log.info("[GC] Expired pod check start");

        try {
            V1PodList podList = kubernetesAdapter.getPodList();

            for (V1Pod pod : podList.getItems()) {
                String podName = pod.getMetadata().getName();

                try {
                    Optional<Long> lastRequestTimestampOpt = kubernetesAdapter.getLatestRequestTimestamp(podName);
                    if (lastRequestTimestampOpt.isEmpty()) {
                        log.warn("[GC] Pod {} has no valid access time in logs.", podName);
                        continue;
                    }

                    long lastRequestTimestamp = lastRequestTimestampOpt.get();
                    long elapsed = Duration.between(Instant.ofEpochMilli(lastRequestTimestamp), Instant.now()).getSeconds();

                    if (elapsed > TTL_SECONDS) {
                        log.info("[GC] Deleting pod: {} (elapsed={}s > ttl={}s)", podName, elapsed, TTL_SECONDS);
                        kubernetesAdapter.deletePod(podName);
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

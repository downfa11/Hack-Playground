package com.ns.solve.service.core;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
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

    private static final long LAST_TTL_SECONDS = 30 * 60; // 30min
    private static final long CREATED_TTL_SECONDS = 3 * 3600; // 3hour
    private static final long DELAYED_PENDING_STATUS_SECONDS = 5 * 60; // 5min
    private static final String WARGAME_NAMESPACE = "wargame";

    private final KubernetesService kubernetesService;


    @Scheduled(fixedDelay = 10 * 60_000)
    @SchedulerLock(name = "gcExpiredK8sPods", lockAtLeastFor = "PT30S", lockAtMostFor = "PT4M")
    public void cleanPods() {
        log.info("[GC] Expired pod check start");

        try {
            V1PodList podList = kubernetesService.getPodList(WARGAME_NAMESPACE);

            for (V1Pod pod : podList.getItems()) {
                Map<String, String> label = pod.getMetadata().getLabels();
                String userId = label.get("userId");
                String problemId = label.get("problemId");

                String labelSelector = String.format("userId=%s,problemId=%s", userId, problemId);
                cleanStuckPod(pod, labelSelector);
                cleanExpiredPod(pod, labelSelector);
            }
        } catch (ApiException e) {
            log.error("[GC] Failed to list pods: {}", e.getResponseBody());
        }
    }

    private void cleanStuckPod(V1Pod pod, String labelSelector) {
        String podName = pod.getMetadata().getName();
        String phase = Optional.ofNullable(pod.getStatus().getPhase()).orElse("Unknown");

        try {
            if (phase.equalsIgnoreCase("Failed") || phase.equalsIgnoreCase("Unknown")) {
                log.warn("[GC] Pod {} is in abnormal phase '{}', force deleting. label={}", podName, phase, labelSelector);
                kubernetesService.forceDeletePod(WARGAME_NAMESPACE, podName);
                return;
            }

            if (phase.equalsIgnoreCase("Pending")) {
                Instant creationTime = pod.getMetadata().getCreationTimestamp().toInstant();
                long elapsedPending = Duration.between(creationTime, Instant.now()).getSeconds();

                if (elapsedPending > DELAYED_PENDING_STATUS_SECONDS) { // 5분 이상 Pending인 경우
                    log.warn("[GC] Pod {} in Pending for {}s, force deleting. {}", podName, elapsedPending, labelSelector);
                    kubernetesService.forceDeletePod(WARGAME_NAMESPACE, podName);
                }
            }
        } catch (Exception e) {
            log.error("[GC] Failed to check or delete abnormal pod {}: {}", podName, e.getMessage());
        }
    }

    private void cleanExpiredPod(V1Pod pod, String labelSelector) {
        String podName = pod.getMetadata().getName();

        Optional<Long> lastRequestTimestampOpt = getLastRequestTimestamp(pod);
        if (lastRequestTimestampOpt.isEmpty()) {
            log.warn("[GC] Pod {} has no valid access time in logs. label: {}", podName, labelSelector);
            deletePodByLabel(labelSelector);
            return;
        }

        long lastRequestTimestamp = lastRequestTimestampOpt.get();
        if (isExpiredByLastRequest(lastRequestTimestamp)) {
            log.info("[GC] Pod {} is expired by last request TTL. Deleting...", podName);
            deletePodByLabel(labelSelector);
            return;
        }

        Instant creationTime = pod.getMetadata().getCreationTimestamp().toInstant();
        if (isExpiredByCreation(creationTime)) {
            log.info("[GC] Pod {} is expired by creation TTL. Deleting...", podName);
            deletePodByLabel(labelSelector);
            return;
        }

        log.info("[GC] Pod {} is still within TTL limits, skipping...", podName);
    }


    private Optional<Long> getLastRequestTimestamp(V1Pod pod) {
        String podName = pod.getMetadata().getName();
        try {
            return kubernetesService.getLatestRequestTimestamp(WARGAME_NAMESPACE, podName);
        } catch (Exception e) {
            log.error("[GC] Failed to get last request timestamp for pod {}: {}", podName, e.getMessage());
            return Optional.empty();
        }
    }

    private boolean isExpiredByCreation(Instant creationTime) {
        long elapsedSinceCreation = Duration.between(creationTime, Instant.now()).getSeconds();
        return elapsedSinceCreation > CREATED_TTL_SECONDS;
    }

    private boolean isExpiredByLastRequest(long lastRequestTimestamp) {
        long elapsed = Duration.between(Instant.ofEpochMilli(lastRequestTimestamp), Instant.now()).getSeconds();
        return elapsed > LAST_TTL_SECONDS;
    }

    private void deletePodByLabel(String labelSelector) {
        try {
            kubernetesService.deleteAllResourcesByLabel(WARGAME_NAMESPACE, labelSelector);
        } catch (Exception e) {
            log.error("[GC] Failed to delete pod with label {}: {}", labelSelector, e.getMessage());
        }
    }


}

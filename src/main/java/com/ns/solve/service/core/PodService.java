package com.ns.solve.service.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ns.solve.service.problem.ProblemService;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ns.solve.service.core.PodBuilder.getPodName;

@Slf4j
@Service
@RequiredArgsConstructor
public class PodService {
    private static final String TRAEFIK_LOG_PATH = "/var/log/traefik/access.log";

    private final KubernetesAdapter kubernetesAdapter;
    private final ProblemService problemService;

    public String createProblemPod(Long userId, Long problemId) {
        String podName = PodBuilder.getPodName(userId, problemId);
        String image = problemService.getImageForProblem(problemId);
        return ensureRunningOrCreate(podName, image);
    }

    /**
     * Pod이 존재하면 상태에 따라 처리하고, 없으면 새로 생성한다.
     * 실패 시 null 반환.
     */
    public String ensureRunningOrCreate(String podName, String image) {
        try {
            Optional<String> phaseOpt = kubernetesAdapter.getPodPhase(podName);
            if (phaseOpt.isPresent()) {
                return handleExistingPod(podName, phaseOpt.get());
            }

            return createAndExposePod(podName, image);
        } catch (Exception e) {
            log.error("Failed to ensureRunningOrCreate {}: {}", podName, e.getMessage(), e);
            return null;
        }
    }

    private String handleExistingPod(String podName, String phase) {

        if ("Running".equals(phase)) {
            if (kubernetesAdapter.isPodReady(podName)) {
                log.info("Pod {} - running and ready.", podName);
                return kubernetesAdapter.getExternalUrl(podName, true); // todo. 일단 대충 shared 문제
            } else {
                log.warn("Pod {} - running... but not ready.", podName);
                return null;
            }
        }

        log.warn("Pod {} - phase: {}.", podName, phase);
        return null;
    }

    private String createAndExposePod(String podName, String image) throws ApiException {
        log.info("Creating pod {} with image {}", podName, image);
        kubernetesAdapter.createPod(podName, image, null, null);

        if (kubernetesAdapter.waitPodToReady(podName, 60)) {
            return kubernetesAdapter.getExternalUrl(podName, true); // todo. 일단 대충 shared 문제
        } else {
            log.error("createAndExposePod Timeout - Pod {} failed to waitPodToReady", podName);
            return null;
        }
    }

    /**
     * 현재 dedicated 유형 문제를 푸는 사용자 목록 조회
     */
    public Map<String, String> findCurrentSolveMember() {
        try {
            V1PodList pods = kubernetesAdapter.getPodsByLabelSelector("app=solve,type=dedicated");

            return pods.getItems().stream()
                    .map(pod -> {
                        Map<String, String> labels = pod.getMetadata().getLabels();
                        return Map.entry(labels.get("userId"), labels.get("problemId"));
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));

        } catch (Exception e) {
            log.error("fetch pod list failed : {}", e.getMessage(), e);
            return Map.of();
        }
    }

    /**
     * Pod를 외부 노출 (Service + Ingress 생성)
     */
    public String exposePod(Long userId, Long problemId, boolean isWebProblem) {
        String podName = getPodName(userId, problemId);

        try {
            V1Service service = PodBuilder.buildService(podName);
            kubernetesAdapter.createService(service);

            if (isWebProblem) {
                Map<String, Object> ingressRoute = PodBuilder.buildIngressRoute(podName);
                kubernetesAdapter.createIngressRoute(ingressRoute);
            }

            return kubernetesAdapter.getExternalUrl(podName, isWebProblem);
        } catch (Exception e) {
            log.error("exposePod Failed  {}: {}", podName, e.getMessage(), e);
            return null;
        }
    }
}



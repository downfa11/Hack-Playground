package com.ns.solve.service.core;

import com.ns.solve.domain.entity.problem.ContainerResourceType;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.domain.entity.problem.WargameKind;
import com.ns.solve.domain.entity.problem.WargameProblem;
import com.ns.solve.service.UserService;
import com.ns.solve.service.problem.ProblemService;
import com.ns.solve.utils.exception.ErrorCode.UserErrorCode;
import com.ns.solve.utils.exception.SolvedException;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ns.solve.service.core.PodBuilder.getPodName;

@Slf4j
@Service
@RequiredArgsConstructor
public class PodService {
    @Value("${server.url}")
    private String serverUrl;

    private final KubernetesService kubernetesService;
    private final ProblemService problemService;
    private final UserService userService;



    public String createProblemPod(Long userId, Long problemId) {
        Problem problem = problemService.getProblemById(problemId);
        if (!problem.getIsChecked()) {
            return "Problem is not checked yet";
        }

        String namespace = problem.getType().getTypeName(); // wargame
        V1PodList podList = kubernetesService.getPodsByLabelSelector(namespace, String.format("userId={}",userId));
        if (!podList.getItems().isEmpty()) {
            return "Another problem container is already running";
        }

        userService.getUserById(userId)
                .orElseThrow(() -> new SolvedException(UserErrorCode.USER_NOT_FOUND));

        String type = problem.getType().getTypeName();
        if ("wargame".equals(type)) {
            WargameProblem wargameProblem = (WargameProblem) problem;
            return ensureRunningOrCreate(wargameProblem, userId);
        }

        return "Invalid Problem type: " + type;
    }


    /**
     * Pod이 존재하면 상태에 따라 처리하고, 없으면 새로 생성한다.
     * 실패 시 null 반환.
     */
    public String ensureRunningOrCreate(WargameProblem wargameProblem, Long userId) {
        String namespace = wargameProblem.getType().getTypeName();
        String podName = getPodName(userId, wargameProblem.getId());

        try {
            Optional<String> phaseOpt = kubernetesService.getPodPhase(namespace, podName);
            if (phaseOpt.isPresent()) {
                return handleExistingPod(namespace, podName, phaseOpt.get());
            }

            return createAndExposePod(wargameProblem, userId);
        } catch (Exception e) {
            log.error("Failed to ensureRunningOrCreate {}: {}", podName, e.getMessage(), e);
            return "Error creating pod";
        }
    }

    private String handleExistingPod(String namespace, String podName, String phase) {
        if ("Running".equals(phase) && !kubernetesService.isPodReady(namespace, podName)) {
            return "Pod is not ready yet.";
        }

        log.warn("Pod {} is in phase: {}", podName, phase);
        return "Pod is not running.";
    }

    private String createAndExposePod(WargameProblem wargameProblem, Long userId) throws ApiException {
        Long problemId = wargameProblem.getId();
        String namespace = wargameProblem.getType().getTypeName();
        Integer port = wargameProblem.getPortNumber();
        ContainerResourceType containerResourceType = wargameProblem.getContainerResourceType();
        Map<String, Integer> resourceLimits = wargameProblem.getResourceLimit();
        String image = wargameProblem.getDockerfileLink();
        WargameKind kind = wargameProblem.getKind();

        kubernetesService.createPod(userId, problemId, namespace, image, resourceLimits);
        String podName = getPodName(userId, problemId);

        if (kubernetesService.waitPodToReady(namespace, podName, 60)) {
            return exposePod(userId, problemId, namespace, kind, port, containerResourceType);
        } else {
            return "createAndExposePod error - timed out.";
        }
    }

    // Traefik Ingress Route 스타일의 External URL 반환
    public String getExternalUrl(Long problemId, String uuid, ContainerResourceType containerResourceType) {
        String dedicatedUrl = String.format("%s/problems/%d/%s/", serverUrl, problemId, uuid);
        String sharedUrl = String.format("%s/problems/%d/%s/", serverUrl, problemId, uuid);

        log.info("getExternalUrl: "+ dedicatedUrl +", " + sharedUrl);
        return containerResourceType==ContainerResourceType.SHARED ? sharedUrl : dedicatedUrl;
    }


    /**
     * 현재 dedicated 유형 문제를 푸는 사용자 목록 조회
     */
    public Map<String, String> findCurrentSolveMember(String namespace) {
        try {
            V1PodList pods = kubernetesService.getPodList(namespace);
            return pods.getItems().stream()
                    .map(pod -> {
                        Map<String, String> labels = pod.getMetadata().getLabels();
                        String userId = labels.get("userId");
                        String problemId = labels.get("problemId");

                        if (userId == null || problemId == null) {
                            return null;
                        }

                        return Map.entry(userId, problemId);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));

        } catch (Exception e) {
            log.error("fetch pod list failed : {}", e.getMessage(), e);
            return Map.of();
        }
    }


    /**
     * Pod를 외부 노출 (Service + Ingress 생성)
     */
    public String exposePod(Long userId, Long problemId, String namespace, WargameKind kind, Integer port, ContainerResourceType containerResourceType) {
        try {
            V1Service service = PodBuilder.buildService(userId, problemId, port);
            kubernetesService.createService(namespace, service);

            String uuid = UUID.randomUUID().toString();
            String url = getExternalUrl(problemId, uuid, containerResourceType);

            if (kind.equals(WargameKind.WEBHACKING)) {
                Map<String, Object> ingressRoute = PodBuilder.buildIngressRoute(userId, problemId, port, namespace);
                kubernetesService.createIngressRoute(namespace, ingressRoute);
            }

            return url;
        } catch (Exception e) {
            log.error("exposePod Failed  {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }
}



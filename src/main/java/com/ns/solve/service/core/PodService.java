package com.ns.solve.service.core;

import com.ns.solve.domain.dto.problem.SolveInfo;
import com.ns.solve.domain.entity.problem.ContainerResourceType;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.domain.entity.problem.WargameKind;
import com.ns.solve.domain.entity.problem.WargameProblem;
import com.ns.solve.service.UserService;
import com.ns.solve.service.problem.ProblemService;
import com.ns.solve.utils.exception.ErrorCode.BaseErrorCode;
import com.ns.solve.utils.exception.ErrorCode.PodErrorCode;
import com.ns.solve.utils.exception.ErrorCode.ProblemErrorCode;
import com.ns.solve.utils.exception.ErrorCode.UserErrorCode;
import com.ns.solve.utils.exception.SolvedException;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        Problem problem = validateUserAndProblemChecked(userId, problemId);
        String namespace = problem.getType().getTypeName();
        V1PodList podList = kubernetesService.getPodsByLabelSelector(namespace, String.format("userId=%s", userId));

        Optional<String> existingUrl = getExistingPodUrlIfExists(podList, userId, problemId, problem);
        if (existingUrl.isPresent()) {
            return existingUrl.get();
        }

        if (isOtherProblemPodRunning(podList)) {
            return "Another problem container is already running";
        }

        return handleProblemType(problem, userId);
    }

    private Problem validateUserAndProblemChecked(Long userId, Long problemId) {
        userService.getUserById(userId)
                .orElseThrow(() -> new SolvedException(UserErrorCode.USER_NOT_FOUND));

        Problem problem = problemService.getProblemById(problemId);
        if (!problem.getIsChecked()) {
            throw new SolvedException(ProblemErrorCode.ACCESS_DENIED);
        }

        return problem;
    }

    private Optional<String> getExistingPodUrlIfExists(V1PodList podList, Long userId, Long problemId, Problem problem) {
        return findMatchingPod(podList, userId, problemId)
                .flatMap(pod -> resolveIngressRouteUrl(problem, userId, problemId));
    }

    private Optional<V1Pod> findMatchingPod(V1PodList podList, Long userId, Long problemId) {
        return podList.getItems().stream()
                .filter(pod -> {
                    Map<String, String> labels = pod.getMetadata().getLabels();
                    return labels != null &&
                            String.valueOf(userId).equals(labels.get("userId")) &&
                            String.valueOf(problemId).equals(labels.get("problemId"));
                })
                .findFirst();
    }

    private Optional<String> resolveIngressRouteUrl(Problem problem, Long userId, Long problemId) {
        String namespace = problem.getType().getTypeName();
        String podName = PodBuilder.getPodName(userId, problemId);

        Map<String, Object> ingressRoute;
        try {
            ingressRoute = kubernetesService.getIngressRouteByName(namespace, podName);
        } catch (ApiException e) {
            throw new SolvedException(ProblemErrorCode.INVALID_PROBLEM_OPERATION);
        }

        if (ingressRoute == null) return Optional.empty();

        Map<String, Object> spec = (Map<String, Object>) ingressRoute.get("spec");
        if (spec == null) return Optional.empty();

        List<Map<String, Object>> routes = (List<Map<String, Object>>) spec.get("routes");
        if (routes == null) return Optional.empty();

        for (Map<String, Object> route : routes) {
            String match = (String) route.get("match");
            Matcher matcher = Pattern.compile("^PathPrefix\\(`/problems/\\d+/([a-f0-9\\-]+)`\\)").matcher(match);

            if (matcher.find()) {
                String uuid = matcher.group(1);
                return Optional.of(getExternalUrl(problemId, uuid, problem.getContainerResourceType()));
            }
        }

        return Optional.empty();
    }


    private boolean isOtherProblemPodRunning(V1PodList podList) {
        return !podList.getItems().isEmpty();
    }

    private String handleProblemType(Problem problem, Long userId) {
        String type = problem.getType().getTypeName();
        if ("wargame".equals(type)) {
            return ensureRunningOrCreate((WargameProblem) problem, userId);
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


    public List<SolveInfo> findCurrentSolveMembers(String namespace) {
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

                        return new SolveInfo(userId, problemId);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("fetch pod list failed : {}", e.getMessage(), e);
            return List.of();
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
                kubernetesService.createStripPrefixMiddleware(namespace, userId, problemId, uuid);
                Map<String, Object> ingressRoute = PodBuilder.buildIngressRoute(userId, problemId, port, namespace, uuid);
                kubernetesService.createIngressRoute(namespace, ingressRoute);
            }

            return url;
        } catch (Exception e) {
            log.error("exposePod Failed  {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }

    public String deleteProblemPod(Long userId, Long problemId) {
        Problem problem = problemService.getProblemById(problemId);
        userService.getUserById(userId).orElseThrow(() -> new SolvedException(UserErrorCode.USER_NOT_FOUND));

        String namespace = problem.getType().getTypeName();
        String labelSelector = String.format("userId=%s,problemId=%s",userId, problemId);

        try {
            List<V1Service> services = kubernetesService.getServicesByLabelSelector(namespace, labelSelector);
            if (services.isEmpty()) {
                throw new SolvedException(PodErrorCode.SERVICE_NOT_FOUND);
            }
        } catch (ApiException e) {
            throw new SolvedException(PodErrorCode.K8S_API_ERROR, e.getMessage());
        }

        try {
            kubernetesService.deleteAllResourcesByLabel(namespace, labelSelector);
        } catch (ApiException e) {
            throw new SolvedException(PodErrorCode.K8S_API_ERROR, e.getMessage());
        }

        return "Success";
    }
}



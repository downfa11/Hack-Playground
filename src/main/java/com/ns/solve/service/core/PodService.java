package com.ns.solve.service.core;

import com.ns.solve.domain.dto.problem.SolveInfo;
import com.ns.solve.domain.entity.contest.ContestProblem;
import com.ns.solve.domain.entity.problem.ContainerResourceType;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.domain.entity.problem.WargameProblem;
import com.ns.solve.domain.vo.ProblemType;
import com.ns.solve.domain.vo.WargameKind;
import com.ns.solve.service.UserService;
import com.ns.solve.service.problem.ProblemService;
import com.ns.solve.utils.exception.ErrorCode.PodErrorCode;
import com.ns.solve.utils.exception.ErrorCode.ProblemErrorCode;
import com.ns.solve.utils.exception.ErrorCode.UserErrorCode;
import com.ns.solve.utils.exception.SolvedException;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
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

    @Value("${server.ip}")
    private String serverIp;

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
            deleteProblemPod(userId, namespace);
        }

        return handleProblemType(problem, userId);
    }

    private Problem validateUserAndProblemChecked(Long userId, Long problemId) {
        userService.getUserById(userId)
                .orElseThrow(() -> new SolvedException(UserErrorCode.USER_NOT_FOUND));

        Problem problem = problemService.getProblemById(problemId);
        if (problem instanceof WargameProblem wargameProblem && !problem.getIsChecked()) {
            throw new SolvedException(ProblemErrorCode.ACCESS_DENIED);
        }
        if (!(problem instanceof WargameProblem) && !(problem instanceof ContestProblem)) {
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
                return Optional.of(String.format("%s/problems/%d/%s/", serverUrl, problemId, matcher.group(1)));
            }
        }

        return Optional.empty();
    }

    // podList의 개수가 1개 이상이면 createProblemPod를 금지한다.
    private boolean isOtherProblemPodRunning(V1PodList podList) {
        long runningCount = podList.getItems().stream()
                .filter(pod -> "Running".equals(pod.getStatus().getPhase()))
                .count();
        return runningCount >= 1;
    }


    private String handleProblemType(Problem problem, Long userId) {
        String type = problem.getType().getTypeName();
        if ("wargame".equals(type)) {
            if (problem instanceof WargameProblem wargameProblem) {
                return ensureRunningOrCreate(wargameProblem, userId);
            } else if (problem instanceof ContestProblem contestProblem) {
                return ensureRunningOrCreate(contestProblem, userId);
            }
            return "Invalid Wargame type: " + type;
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
                return handleExistingPod(namespace, podName, phaseOpt.get(), userId, wargameProblem.getId(), wargameProblem.getKind(), wargameProblem.getPortNumber(), wargameProblem.getContainerResourceType());
            }

            return createAndExposePod(wargameProblem, userId);
        } catch (Exception e) {
            log.error("Failed to ensureRunningOrCreate {}: {}", podName, e.getMessage(), e);
            return "Error creating pod";
        }
    }

    public String ensureRunningOrCreate(ContestProblem contestProblem, Long userId) {
        String namespace = contestProblem.getType().getTypeName();
        String podName = getPodName(userId, contestProblem.getId());

        try {
            Optional<String> phaseOpt = kubernetesService.getPodPhase(namespace, podName);
            if (phaseOpt.isPresent()) {
                return handleExistingPod(namespace, podName, phaseOpt.get(), userId, contestProblem.getId(), contestProblem.getKind(), contestProblem.getPortNumber(), contestProblem.getContainerResourceType());
            }

            return createAndExposePod(contestProblem, userId);
        } catch (Exception e) {
            log.error("Failed to ensureRunningOrCreate {}: {}", podName, e.getMessage(), e);
            return "Error creating pod";
        }
    }


    private String handleExistingPod(String namespace, String podName, String phase, Long userId, Long problemId, WargameKind kind, Integer port, ContainerResourceType containerResourceType) {
        if ("Running".equals(phase) && kubernetesService.isPodReady(namespace, podName)){
            // 멀쩡한 경우
            Problem problem = problemService.getProblemById(problemId);
            Optional<String> existingUrl = resolveIngressRouteUrl(problem, userId, problemId);
            if (existingUrl.isPresent()) {
                log.info("Resolved existing URL. pod {}'s url {}", podName, existingUrl.get());
                return existingUrl.get();
            }
        }

        log.warn("Pod {} is in phase: {}. Cannot expose.", podName, phase);
        return String.format("Pod phase: %s. Please try again later or contact support.", phase);
    }

    private String createAndExposePod(WargameProblem wargameProblem, Long userId) throws ApiException {
        Long problemId = wargameProblem.getId();
        String namespace = wargameProblem.getType().getTypeName();
        Integer port = wargameProblem.getPortNumber();
        Map<String, Integer> resourceLimits = wargameProblem.getResourceLimit();
        String image = wargameProblem.getDockerfileLink();
        WargameKind kind = wargameProblem.getKind();

        // 1. 모든 문제에서 Service 먼저 생성
        if(wargameProblem.getKind().equals(WargameKind.WEBHACKING)){
            port = 18889; // todo. Web 문제인 경우에는 detache의 Port를 trace
        }
        V1Service service = PodBuilder.buildService(userId, problemId, kind, port);
        kubernetesService.createService(namespace, service);

        // 2. 생성된 Service 조회 후 NodePort 확보
        String serviceName = service.getMetadata().getName();
        V1Service createdService = kubernetesService.getService(namespace, serviceName);
        Integer nodePort = createdService.getSpec().getPorts().get(0).getNodePort();

        // 3. Pod 생성 (NodePort 전달)
        kubernetesService.createPod(userId, problemId, port, nodePort, kind, namespace, image, resourceLimits);
        String podName = getPodName(userId, problemId);

        if (!kubernetesService.waitPodToReady(namespace, podName, 60)) {
            try {
                kubernetesService.deleteService(namespace, serviceName); // pod 장애시 service 리소스 회수
            } catch (Exception rollbackException) {
                log.error("Rollback failed for service {} after pod creation timeout: {}", serviceName, rollbackException.getMessage());
            }
            return "createAndExposePod error - timed out.";
        }

        // 4. Pod Ready 후 URL 반환
        return exposePod(userId, problemId, namespace, kind, port, nodePort);
    }

    private String createAndExposePod(ContestProblem contestProblem, Long userId) throws ApiException {
        Long problemId = contestProblem.getId();
        String namespace = contestProblem.getType().getTypeName();
        Integer port = contestProblem.getPortNumber();
        Map<String, Integer> resourceLimits = contestProblem.getResourceLimit();
        String image = contestProblem.getDockerfileLink();
        WargameKind kind = contestProblem.getKind();

        // 1. 모든 문제에서 Service 먼저 생성
        if(contestProblem.getKind().equals(WargameKind.WEBHACKING)){
            port = 18889; // todo. Web 문제인 경우에는 detache의 Port를 trace
        }
        V1Service service = PodBuilder.buildService(userId, problemId, kind, port);
        kubernetesService.createService(namespace, service);

        // 2. 생성된 Service 조회 후 NodePort 확보
        String serviceName = service.getMetadata().getName();
        V1Service createdService = kubernetesService.getService(namespace, serviceName);
        Integer nodePort = createdService.getSpec().getPorts().get(0).getNodePort();

        // 3. Pod 생성 (NodePort 전달)
        kubernetesService.createPod(userId, problemId, port, nodePort, kind, namespace, image, resourceLimits);
        String podName = getPodName(userId, problemId);

        if (!kubernetesService.waitPodToReady(namespace, podName, 60)) {
            try {
                kubernetesService.deleteService(namespace, serviceName); // pod 장애시 service 리소스 회수
            } catch (Exception rollbackException) {
                log.error("Rollback failed for service {} after pod creation timeout: {}", serviceName, rollbackException.getMessage());
            }
            return "createAndExposePod error - timed out.";
        }

        // 4. Pod Ready 후 URL 반환
        return exposePod(userId, problemId, namespace, kind, port, nodePort);
    }

    /**
     * Pod를 외부 노출 (Service + Ingress 생성)
     * NodePort는 필요 시 전달
     */
    public String exposePod(Long userId, Long problemId, String namespace, WargameKind kind, Integer port, Integer nodePort) {
        try {
            String uuid = UUID.randomUUID().toString();
            String url;

            if (kind.equals(WargameKind.WEBHACKING)) {
                // WEBHACKING은 IngressRoute 생성
                kubernetesService.createStripPrefixMiddleware(namespace, userId, problemId, uuid);
                Map<String, Object> ingressRoute = PodBuilder.buildIngressRoute(userId, problemId, port, namespace, uuid);
                kubernetesService.createIngressRoute(namespace, ingressRoute);

                url = String.format("%s:30000/problems/%d/%s/", serverUrl, problemId, uuid); // todo. KOREN망에서만 30000, 운영시 80으로 생략
            } else {
                // SYSTEM / REVERSING은 NodePort 접속
                url = String.format("nc %s %d", serverIp, nodePort);
            }

            return url;
        } catch (Exception e) {
            log.error("exposePod Failed  {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }


    /**
     * 현재 dedicated 유형 문제를 푸는 사용자 목록 조회
     */
    public List<SolveInfo> findCurrentSolveMembers(String namespace) {
        try {
            V1ServiceList services = kubernetesService.getServiceList(namespace);

            Map<String, List<String>> problemToNicknames = new HashMap<>();

            for (var service : services.getItems()) {
                Map<String, String> labels = service.getMetadata().getLabels();
                String userId = labels.get("userId");
                String problemId = labels.get("problemId");

                if (userId == null || problemId == null) {
                    continue;
                }

                String nickname = userService.getNicknameByUserId(userId);
                if (nickname == null) {
                    continue;
                }

                problemToNicknames
                        .computeIfAbsent(problemId, k -> new ArrayList<>())
                        .add(nickname);
            }

            return problemToNicknames.entrySet().stream()
                    .map(entry -> {
                        String problemId = entry.getKey();
                        List<String> nicknames = entry.getValue();
                        String title = problemService.getProblemTitleById(problemId);
                        return new SolveInfo(title, nicknames);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("fetch pod list failed : {}", e.getMessage(), e);
            return List.of();
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

    public String deleteProblemPod(Long userId, String namespace) {
        String labelSelector = String.format("userId=%s",userId);

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

    // ---

    public String createProblemAndGetUrl(String image, boolean isHttp) {
        Random random = new Random();
        Long userId = 1000000L + random.nextInt(1000000);
        Long problemId = 1000000L + random.nextInt(1000000);

        WargameKind kind = isHttp ? WargameKind.WEBHACKING : WargameKind.SYSTEM;
        WargameProblem tempProblem = buildTemporaryWargameProblem(problemId, image, kind);
        String podName = getPodName(userId, problemId);

        try {
            Optional<String> phaseOpt = kubernetesService.getPodPhase("wargame", podName);
            if (phaseOpt.isPresent()) {
                throw new SolvedException(ProblemErrorCode.INVALID_PROBLEM_OPERATION,
                        String.format("Pod already exists for generated userId(%d) and problemId(%d). Please retry.", userId, problemId));
            }

            log.info("Creating K8s resources for temporary problem. userId: {}, problemId: {}, image: {}", userId, problemId, image);
            return createAndExposePod(tempProblem, userId);
        } catch (SolvedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to createProblemAndGetUrl {}: {}", podName, e.getMessage(), e);
            return "Error creating problem environment: " + e.getMessage();
        }
    }

    private WargameProblem buildTemporaryWargameProblem(Long problemId, String image, WargameKind kind) {
        WargameProblem problem = new WargameProblem();
        problem.setId(problemId);
        problem.setDockerfileLink(image);
        problem.setKind(kind);
        problem.setPortNumber(80);
        problem.setType(ProblemType.WARGAME);

        Map<String, Integer> resourceLimits = Map.of("cpu", 1000, "memory", 512);
        problem.setResourceLimit(resourceLimits);

        return problem;
    }
}



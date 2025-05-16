package com.ns.solve.service.problem;

import com.ns.solve.domain.dto.problem.*;
import com.ns.solve.domain.dto.problem.algorithm.RegisterAlgorithmProblemDto;
import com.ns.solve.domain.dto.problem.wargame.RegisterWargameProblemDto;
import com.ns.solve.domain.dto.problem.wargame.WargameProblemDto;
import com.ns.solve.domain.dto.user.UserFirstBloodDto;
import com.ns.solve.domain.entity.Solved;
import com.ns.solve.domain.entity.User;
import com.ns.solve.domain.entity.problem.*;
import com.ns.solve.domain.vo.FileInfo;
import com.ns.solve.repository.SolvedRepository;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.repository.problem.ProblemRepository;
import com.ns.solve.service.FileService;
import com.ns.solve.service.UserService;
import com.ns.solve.utils.exception.ErrorCode.ProblemErrorCode;
import com.ns.solve.utils.exception.ErrorCode.UserErrorCode;
import com.ns.solve.utils.exception.SolvedException;
import com.ns.solve.utils.mapper.ProblemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProblemService {
    private final FileService fileService;

    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;

    private final SolvedRepository solvedRepository;


    /*
     * 1. Assignment에 Github 주소를 제출한다.
     * 2. 해당 Problem의 Case 들을 불러온다.
     * 3. k8s 환경에서 빌드하고 Case들을 입력한다.
     * 4. 입력에 대한 결과들을 저장하고 정답과 비교, 평가한다. (케이스별 가중치)
     */

    @Transactional
    public ProblemDto createProblem(Long userId, RegisterProblemDto registerProblemDto) {
        Problem problem = createProblemByType(registerProblemDto);
        setCommonProblemFields(problem, userId, registerProblemDto);
        Problem saved = problemRepository.save(problem);
        return ProblemMapper.mapperToProblemDto(saved);
    }

    private Problem createProblemByType(RegisterProblemDto registerProblemDto) {
        if (registerProblemDto instanceof RegisterWargameProblemDto wargameDto) {
            return createWargameProblem(wargameDto);
        } else if (registerProblemDto instanceof RegisterAlgorithmProblemDto algorithmDto) {
            return new AlgorithmProblem();
        } else {
            throw new SolvedException(ProblemErrorCode.INVALID_PROBLEM_OPERATION, "type: " + registerProblemDto.getClass().getName());
        }
    }


    private WargameProblem createWargameProblem(RegisterWargameProblemDto wargameDto) {
        WargameProblem wargameProblem = new WargameProblem();
        wargameProblem.setType(ProblemType.WARGAME);
        wargameProblem.setKind(wargameDto.getKind());
        wargameProblem.setLevel(wargameDto.getLevel());
        wargameProblem.setFlag(wargameDto.getFlag());
        wargameProblem.setDockerfileLink(wargameDto.getDockerfileLink());

        return wargameProblem;
    }

    private void setCommonProblemFields(Problem problem, Long userId, RegisterProblemDto registerProblemDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "userId: " + userId));
        problem.setCreator(user);
        problem.setCorrectCount(0.0);
        problem.setEntireCount(0.0);
        problem.setTitle(registerProblemDto.getTitle());
        problem.setDetail(registerProblemDto.getDetail());
        problem.setTags(registerProblemDto.getTags());
    }

    private void setCommonProblemFields(Problem problem, Long userId, ModifyProblemDto modifyProblemDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "userId: " + userId));
        problem.setCreator(user);

        problem.setType(modifyProblemDto.getType());
        problem.setTitle(modifyProblemDto.getTitle());
        problem.setDetail(modifyProblemDto.getDetail());
        problem.setTags(modifyProblemDto.getTags());
    }


    public void uploadFile(Long userId, Long problemId, MultipartFile file) {
        Problem problem = problemRepository.findById(problemId).orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "problemId: " + problemId));
        User user = userRepository.findById(userId).orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "userId: " + userId));

        checkAuthorizationOrThrow(user, problem);
        handleFileUpload(file, problem);
    }

    // Wargame일때 업로드할 파일을 동봉했는지 확인하고, 저장-관리합니다.
    private void handleFileUpload(MultipartFile file, Problem savedProblem) {
        if (file != null && !file.isEmpty()) {
            if (savedProblem instanceof WargameProblem wargameProblem) {
                if (wargameProblem.getProblemFile() != null) {
                    fileService.deleteFile(wargameProblem.getProblemFile());
                }

                FileInfo fileInfo = fileService.uploadFile(wargameProblem.getId(), file);
                wargameProblem.setProblemFile(fileInfo.fileName());
                wargameProblem.setProblemFileSize(fileInfo.fileSize());
                problemRepository.save(wargameProblem);
            } else {
                throw new SolvedException(ProblemErrorCode.INVALID_PROBLEM_TYPE, "only supported for wargame");
            }
        }
    }


    @Transactional
    public ProblemDto updateProblem(Long userId, Long problemId, ModifyProblemDto modifyProblemDto) {
        Problem existingProblem = problemRepository.findById(problemId).orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "problemId: " + problemId));
        User user = userRepository.findById(userId).orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "userId: " + userId));

        checkAuthorizationOrThrow(user, existingProblem);
        setCommonProblemFields(existingProblem, userId, modifyProblemDto);
        Problem saved = problemRepository.save(existingProblem);
        updateLastActived(user);
        return ProblemMapper.mapperToProblemDto(saved);
    }

    @Transactional
    public void deleteProblem(Long userId, Long problemId) {
        Problem problem = problemRepository.findById(problemId).orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "problemId: " + problemId));
        User user = userRepository.findById(userId).orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "userId: " + userId));

        checkAuthorizationOrThrow(user, problem);
        updateLastActived(user);
        problemRepository.deleteById(problemId);
    }

    @Transactional
    public ProblemDto createProblemWithFile(Long userId, RegisterProblemDto registerProblemDto, MultipartFile file) {
        Problem problem = createProblemByType(registerProblemDto);

        setCommonProblemFields(problem, userId, registerProblemDto);
        Problem saved = problemRepository.save(problem);
        handleFileUpload(file, saved);
        return ProblemMapper.mapperToProblemDto(saved);
    }


    @Transactional
    public ProblemDto updateProblemWithFile(Long userId, Long problemId, ModifyProblemDto modifyProblemDto, MultipartFile file) {
        Problem existingProblem = problemRepository.findById(problemId)
                .orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "problemId: " + problemId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "userId: " + userId));

        checkAuthorizationOrThrow(user, existingProblem);
        setCommonProblemFields(existingProblem, userId, modifyProblemDto);
        Problem saved = problemRepository.save(existingProblem);
        handleFileUpload(file, saved);
        updateLastActived(user);
        return ProblemMapper.mapperToProblemDto(saved);
    }

    public Optional<ProblemDto> getProblemDtoById(Long id) {
        return problemRepository.findById(id).map(ProblemMapper::mapperToProblemDto);
    }

    public Problem getProblemById(Long id) {
        return problemRepository.findById(id).orElseThrow(()->new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND));
    }

    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    @Transactional
    public WargameProblemDto toggleProblemCheckStatus(Long reviewerId, Long id, ProblemCheckDto problemCheckDto) {
        Problem problem = problemRepository.findById(id).orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "problemId: " + id));
        User user = userRepository.findById(reviewerId).orElseThrow(() -> new SolvedException(UserErrorCode.USER_NOT_FOUND, "User not found: "+ reviewerId));

        if(!user.isMemberAbove()){
            throw new SolvedException(ProblemErrorCode.ACCESS_DENIED, "Authorization: "+user.getRole());
        }

        problem.setIsChecked(!problem.getIsChecked());
        problem.setReviewer(user);

        if (problemCheckDto.getContainerResourceType() != null) {
            problem.setContainerResourceType(problemCheckDto.getContainerResourceType());
        }
        if (problemCheckDto.getPortNumber() != null) {
            problem.setPortNumber(problemCheckDto.getPortNumber());
        }

        Map<String, Integer> resourceLimit = new HashMap<>();
        if (problemCheckDto.getCpuLimit() != null) {
            resourceLimit.put("cpu", problemCheckDto.getCpuLimit());
        }
        if (problemCheckDto.getMemoryLimit() != null) {
            resourceLimit.put("memory", problemCheckDto.getMemoryLimit());
        }
        problem.setResourceLimit(resourceLimit);
        Problem savedProblem = problemRepository.save(problem);

        if (savedProblem instanceof WargameProblem wargameProblem) {
            return ProblemMapper.mapperToWargameProblemDto(wargameProblem);
        }
        throw new SolvedException(ProblemErrorCode.INVALID_PROBLEM_TYPE, "only supported for wargame");
    }

    public WargameProblemDto getWargameProblemById(Long id) {
        Problem problem = problemRepository.findById(id).orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "problemId: " + id));

        if (problem instanceof WargameProblem wargameProblem) {
            return ProblemMapper.mapperToWargameProblemDto(wargameProblem);
        }
        throw new SolvedException(ProblemErrorCode.INVALID_PROBLEM_TYPE, "only supported for wargame");
    }

    public List<WargameProblem> getAllWargameProblems() {
        return problemRepository.findByTypeWargame(ProblemType.WARGAME);
    }

    public Page<Problem> getPendingProblems(PageRequest pageRequest) {
        return problemRepository.findProblemsByStatusPending(pageRequest);
    }

    public Long getChekcedProblemsCount() {
        return problemRepository.countCheckedProblems();
    }

    public Long getNewProblemsCount(LocalDateTime now) {
        return problemRepository.countNewProblems(now);
    }

    private Page<ProblemSummary> getCompletedProblemsByType(ProblemType type, WargameKind kind, String sortKind, boolean desc, PageRequest pageRequest) {
        if (sortKind == null) {
            sortKind = "problemId";
        }

        switch (sortKind) {
            case "updatedAt":
                return problemRepository.findProblemsByStatusAndTypeSortedByUpdatedAt(type, kind, desc, pageRequest);
            case "correctRate":
                return problemRepository.findProblemsByStatusAndTypeSortedByCorrectRate(type, kind, desc, pageRequest);
            default: // problemId
                return problemRepository.findProblemsByStatusAndTypeSortedById(type, kind, desc, pageRequest);
        }
    }

    public Page<ProblemSummary> getCompletedProblemsSummary(Long userId, ProblemType type, WargameKind kind, String sortKind, boolean desc, PageRequest pageRequest) {
        Page<ProblemSummary> problemSummaries = getCompletedProblemsByType(type, kind, sortKind, desc, pageRequest);

        problemSummaries.forEach(summary -> {
            boolean solved = solvedRepository.existsSolvedProblem(userId, summary.getId());
            summary.setSolved(solved);
        });
        return problemSummaries;
    }


    // problemId에 해당하는 문제의 First Blood가 누구인지 조회
    public List<UserFirstBloodDto> firstBlood(Long problemId, int size) {
        return solvedRepository.findFirstBloodByProblemId(problemId, PageRequest.of(0, size));
    }


    @Transactional
    public Boolean solveProblem(Long userId, Long problemId, String attemptedFlag) {
        User user = userRepository.findById(userId).orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "userId: " + userId));
        boolean isCorrect = false;
        Problem problem = problemRepository.findProblemWithLock(problemId);
        if (problem == null) {
            throw new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "problemId: " + problemId);
        }
        else if (!problem.getIsChecked()){
            throw new SolvedException(ProblemErrorCode.ACCESS_DENIED, "problemId: " + problemId);
        }

        // 해당 문제를 푼 적 없는 경우, 사용자의 랭킹을 갱신합니다.
        if (!solvedRepository.existsSolvedProblem(userId, problemId)) {
            // todo. WARGAME 문제가 아니어도 호출된다
            isCorrect = problemRepository.matchFlagToWargameProblem(problemId, attemptedFlag);
            if (isCorrect) {
                if (user.getFieldScores() == null) {
                    user.setFieldScores(new HashMap<>());
                }

                Map<String, Long> fieldScores = user.getFieldScores();
                String fieldKey = problem.getDomainKind()
                        .map(kind -> problem.getType().getTypeName() + "_" + kind.getTypeName())
                        .orElse(problem.getType().name());

                fieldScores.put(fieldKey, fieldScores.getOrDefault(fieldKey, 0L) + 1);
                user.setScore(user.getScore() + 1);
                user.setLastActived(LocalDateTime.now());
                userRepository.save(user);

                problem.incrementCorrectCount();
            }
            problem.incrementEntireCount();
            problemRepository.save(problem);
        }

        Solved solved = new Solved();
        solved.setSolvedUser(user);
        solved.setSolvedProblem(problem);
        solved.setSolve(isCorrect);
        solved.setSolvedTime(LocalDateTime.now());
        solvedRepository.save(solved);

        return isCorrect;
    }

    public Resource downloadProblemFile(Long problemId) {
        try {
            Problem problem = problemRepository.findById(problemId)
                    .orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "problemId: " + problemId));

            if (!(problem instanceof WargameProblem wargameProblem)) {
                throw new SolvedException(ProblemErrorCode.INVALID_PROBLEM_OPERATION, "only supported for wargame");
            }

            if (wargameProblem.getProblemFile() == null) {
                throw new SolvedException(ProblemErrorCode.FILE_NOT_FOUND, "problemId : " + problemId);
            }

            Resource resource = fileService.downloadFile(wargameProblem.getProblemFile());
            if (!resource.exists()) {
                throw new SolvedException(ProblemErrorCode.FILE_NOT_FOUND, wargameProblem.getProblemFile());
            }

            return resource;
        } catch (Exception e) {
            throw new SolvedException(ProblemErrorCode.FILE_UPLOAD_FAILED, "downloadProblemFile failed: " + e.getMessage());
        }
    }

    public String getImageForProblem(Long problemId) {
        WargameProblem problem = (WargameProblem) problemRepository.findById(problemId)
                .orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "problemId: " + problemId));

        return problem.getDockerfileLink();
    }

    public List<String> getSolvedProblemsTitle(Long userId) {
        return solvedRepository.findSolvedProblemTitlesByUserId(userId);
    }


    private void checkAuthorizationOrThrow(User user, Problem problem) {
        if (!user.isMemberAbove() && !problem.getCreator().equals(user)) {
            throw new SolvedException(ProblemErrorCode.ACCESS_DENIED);
        }
    }

    public Page<ProblemSummary> searchProblems(ProblemType type, WargameKind kind, String keyword, Pageable pageable) {
        return problemRepository.searchKeywordInTitle(type, kind, keyword, pageable);
    }
    
    private void updateLastActived(User user){
        user.setLastActived(LocalDateTime.now());
        userRepository.save(user);
    }
}

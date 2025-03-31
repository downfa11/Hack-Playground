package com.ns.solve.service.problem;

import com.ns.solve.domain.Solved;
import com.ns.solve.domain.User;
import com.ns.solve.domain.dto.*;
import com.ns.solve.domain.dto.problem.*;
import com.ns.solve.domain.dto.problem.wargame.RegisterWargameProblemDto;
import com.ns.solve.domain.dto.problem.wargame.WargameProblemDto;
import com.ns.solve.domain.problem.*;
import com.ns.solve.repository.SolvedRepository;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.repository.problem.ProblemRepository;
import com.ns.solve.service.FileService;
import com.ns.solve.utils.ProblemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProblemService {
    private final FileService fileService;

    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final SolvedRepository solvedRepository;


    /*
     * 1. Assignment에 Github 주소를 제출한다.
     * 2. 해당 Problem의 Case 들을 불러온다.
     * 3. k8s 환경에서 빌드하고 Case들을 입력한다.
     * 4. 입력에 대한 결과들을 저장하고 정답과 비교, 평가한다. (케이스별 가중치)
     */

    @Transactional
    public Problem createProblem(RegisterProblemDto registerProblemDto) {
        Problem problem = createProblemByType(registerProblemDto);
        setCommonProblemFields(problem, registerProblemDto);

        return problemRepository.save(problem);
    }

    private Problem createProblemByType(RegisterProblemDto registerProblemDto) {
        if (registerProblemDto instanceof RegisterWargameProblemDto wargameDto) {
            return createWargameProblem(wargameDto);
        } else if (registerProblemDto instanceof RegisterAlgorithmProblemDto algorithmDto) {
            return new AlgorithmProblem();
        } else {
            throw new IllegalArgumentException("Unsupported problem type: " + registerProblemDto.getClass().getName());
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

    private void setCommonProblemFields(Problem problem, RegisterProblemDto registerProblemDto) {
        problem.setTitle(registerProblemDto.getTitle());
        problem.setCreator(registerProblemDto.getCreator());
        problem.setDetail(registerProblemDto.getDetail());
        problem.setTags(registerProblemDto.getTags());
    }

    private void setCommonProblemFields(Problem problem, ModifyProblemDto modifyProblemDto) {
        problem.setType(modifyProblemDto.getType());
        problem.setTitle(modifyProblemDto.getTitle());
        problem.setCreator(modifyProblemDto.getCreator());
        problem.setDetail(modifyProblemDto.getDetail());
        problem.setTags(modifyProblemDto.getTags());
    }


    public void uploadFile(Long problemId, MultipartFile file){
        Problem problem = problemRepository.findById(problemId).orElseThrow(() -> new RuntimeException("Not found problem."));
        handleFileUpload(file, problem);
    }

    // Wargame일때 업로드할 파일을 동봉했는지 확인하고, 저장-관리합니다.
    private void handleFileUpload(MultipartFile file, Problem savedProblem) {
        if (file != null && !file.isEmpty()) {
            if (savedProblem instanceof WargameProblem wargameProblem) {
                if (wargameProblem.getProblemFile() != null) {
                    fileService.deleteFile(wargameProblem.getProblemFile());
                }

                String filePath = fileService.uploadFile(wargameProblem.getId(), file);
                wargameProblem.setProblemFile(filePath);
                problemRepository.save(wargameProblem);
            } else {
                throw new UnsupportedOperationException("File upload is only supported for WargameProblem.");
            }
        }
    }


    @Transactional
    public Problem updateProblem(ModifyProblemDto modifyProblemDto) {
        Problem existingProblem = problemRepository.findById(modifyProblemDto.getProblemId())
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + modifyProblemDto.getProblemId()));

        setCommonProblemFields(existingProblem, modifyProblemDto);
        return problemRepository.save(existingProblem);
    }

    @Transactional
    public void deleteProblem(Long id) {
        problemRepository.deleteById(id);
    }

    @Transactional
    public Problem createProblemWithFile(RegisterProblemDto registerProblemDto, MultipartFile file) {
        Problem problem = createProblemByType(registerProblemDto);

        setCommonProblemFields(problem, registerProblemDto);
        problemRepository.save(problem);
        handleFileUpload(file, problem);
        return problem;
    }


    @Transactional
    public Problem updateProblemWithFile(ModifyProblemDto modifyProblemDto, MultipartFile file) {
        Problem existingProblem = problemRepository.findById(modifyProblemDto.getProblemId())
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + modifyProblemDto.getProblemId()));

        setCommonProblemFields(existingProblem, modifyProblemDto);
        problemRepository.save(existingProblem);

        handleFileUpload(file, existingProblem);
        return existingProblem;
    }



    public Optional<Problem> getProblemById(Long id) {
        return problemRepository.findById(id);
    }

    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    @Transactional
    public WargameProblemDto toggleProblemCheckStatus(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + id));

        problem.setIsChecked(!problem.getIsChecked());
        Problem savedProblem = problemRepository.save(problem);

        if (savedProblem instanceof WargameProblem wargameProblem) {
            return ProblemMapper.toWargameProblemDto(wargameProblem);
        }
        throw new IllegalArgumentException("Not a WargameProblem");
    }

    public WargameProblemDto getWargameProblemById(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + id));

        if (problem instanceof WargameProblem wargameProblem) {
            return ProblemMapper.toWargameProblemDto(wargameProblem);
        }
        throw new IllegalArgumentException("Not of type WargameProblem");
    }

    public List<WargameProblem> getAllWargameProblems() {
        return problemRepository.findByTypeWargame(ProblemType.WARGAME);
    }

    public Page<Problem> getPendingProblems(PageRequest pageRequest) {
        return problemRepository.findProblemsByStatusPending(pageRequest);
    }

    private Page<ProblemSummary> getCompletedProblemsByType(String type, String sortKind, boolean desc, PageRequest pageRequest) {
        switch(sortKind){
            case "updatedAt":
                return problemRepository.findProblemsByStatusAndTypeSortedByUpdatedAt(type, desc, pageRequest);
            case "correctRate":
                return problemRepository.findProblemsByStatusAndTypeSortedByCorrectRate(type, desc, pageRequest);
        }
        return problemRepository.findProblemsByStatusAndTypeSortedById(type, desc, pageRequest);
    }

    public Page<ProblemSummary> getCompletedProblemsSummary(Long userId, String type, String sortKind, boolean desc, PageRequest pageRequest) {
        Page<ProblemSummary> problemSummaries = getCompletedProblemsByType(type, sortKind, desc, pageRequest);

        for (ProblemSummary summary : problemSummaries) {
            boolean solved = solvedRepository.existsSolvedProblem(userId, summary.getId());
            summary.setSolved(solved);
        }
        return problemSummaries;
    }

    // problemId에 해당하는 문제의 First Blood가 누구인지 조회
    public Optional<UserDto> firstBlood(Long problemId, int size) {
        Page<User> firstSolverPage = solvedRepository.findFirstUserToSolveProblem(problemId, PageRequest.of(0, size));

        return firstSolverPage.getContent()
                .stream()
                .findFirst()
                .map(user -> new UserDto(user.getNickname(), user.getScore(), user.getLastActived()));
    }


    @Transactional
    public boolean solveProblem(Long userId, Long problemId, String attemptedFlag) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Problem problem = problemRepository.findProblemWithLock(problemId);
        if (problem == null) {
            throw new IllegalArgumentException("Problem not found: " + problemId);
        }

        boolean isCorrect = problemRepository.matchFlagToProblems(problemId, attemptedFlag);
        if (isCorrect) {
            problem.incrementCorrectCount();

            // 해당 문제를 푼 적 없는 경우, 사용자의 랭킹을 갱신합니다.
            if(!solvedRepository.existsSolvedProblem(userId, problemId)){
                if (user.getFieldScores() == null) {
                    user.setFieldScores(new HashMap<>());
                }

                Map<String, Long> fieldScores = user.getFieldScores();
                String problemType = String.valueOf(problem.getType());

                fieldScores.put(problemType, fieldScores.getOrDefault(problemType, 0L) + 1);
                userRepository.save(user);
            }
        }

        problem.incrementEntireCount();
        problemRepository.save(problem);


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
                    .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + problemId));

            if (!(problem instanceof WargameProblem wargameProblem)) {
                throw new UnsupportedOperationException("File download is only supported for WargameProblem.");
            }

            if (wargameProblem.getProblemFile() == null) {
                throw new FileNotFoundException("No file found for problem: " + problemId);
            }

            Resource resource = fileService.downloadFile(wargameProblem.getProblemFile());
            if (!resource.exists()) {
                throw new FileNotFoundException("File not found: " + wargameProblem.getProblemFile());
            }

            return resource;
        } catch (Exception e) {
            throw new RuntimeException("File download failed", e);
        }
    }
}

package com.ns.solve.service.contest;

import com.ns.solve.domain.dto.contest.ContestProblemDto;
import com.ns.solve.domain.dto.contest.ContestProblemForOrganizerDto;
import com.ns.solve.domain.dto.contest.ModifyContestProblemRequest;
import com.ns.solve.domain.dto.contest.RegisterContestProblemRequest;
import com.ns.solve.domain.dto.user.UserDto;
import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.contest.ContestProblem;
import com.ns.solve.domain.entity.contest.ContestSolved;
import com.ns.solve.domain.entity.contest.Team;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.domain.entity.user.User;
import com.ns.solve.domain.vo.FileInfo;
import com.ns.solve.domain.vo.WargameKind;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.repository.contest.ContestProblemRepository;
import com.ns.solve.repository.contest.ContestRepository;
import com.ns.solve.repository.contest.ContestSolvedRepository;
import com.ns.solve.repository.contest.TeamRepository;
import com.ns.solve.service.FileService;
import com.ns.solve.utils.exception.ErrorCode.*;
import com.ns.solve.utils.exception.SolvedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContestProblemService {
    private final FileService fileService;
    private final UserRepository userRepository;
    private final ContestRepository contestRepository;
    private final ContestProblemRepository contestProblemRepository;
    private final ContestSolvedRepository contestSolvedRepository;
    private final TeamRepository teamRepository;


    @Transactional
    public ContestProblemDto createProblem(Long contestId, Long userId, RegisterContestProblemRequest request, MultipartFile file) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SolvedException(UserErrorCode.USER_NOT_FOUND));

        checkContestOrganizerAuthorizationOrThrow(user, contest);
        if (contestProblemRepository.existsByContestAndTitle(contest, request.getTitle())) {
            throw new SolvedException(ContestProblemErrorCode.DUPLICATE_PROBLEM_TITLE);
        }

        LocalDateTime now = LocalDateTime.now();
        ContestProblem problem = ContestProblem.builder()
                .title(request.getTitle())
                .contest(contest)
                .type(request.getType())
                .creator(user)
                .detail(request.getDetail())
                .tags(request.getTags())
                .points(request.getPoints())
                .kind(request.getKind())
                .flag(request.getFlag())
                .portNumber(request.getPortNumber())
                .difficulty(request.getDifficulty())
                .dockerfileLink(request.getDockerfileLink())
                .entireCount((double) 0)
                .correctCount((double) 0)
                .createdAt(now)
                .updatedAt(now)
                .build();

        ContestProblem newProblem = contestProblemRepository.save(problem);
        handleFileUpload(file, newProblem);
        return convertToDto(newProblem, newProblem.getContest().getTitle(), null);
    }

    @Transactional
    public ContestProblemDto updateProblem(Long problemId, Long userId, ModifyContestProblemRequest request, MultipartFile file) {
        ContestProblem problem = contestProblemRepository.findById(problemId)
                .orElseThrow(() -> new SolvedException(ContestProblemErrorCode.CONTEST_PROBLEM_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SolvedException(UserErrorCode.USER_NOT_FOUND));

        checkContestOrganizerAuthorizationOrThrow(user, problem.getContest());
        if (!problem.getTitle().trim().equals(request.getTitle().trim())
                && contestProblemRepository.existsByContestAndTitle(problem.getContest(), request.getTitle())) {
            throw new SolvedException(ContestProblemErrorCode.DUPLICATE_PROBLEM_TITLE);
        }

        problem.setTitle(request.getTitle());
        problem.setDetail(request.getDetail());
        problem.setPoints(request.getPoints());
        problem.setKind(request.getKind());
        problem.setTags(request.getTags());
        problem.setFlag(request.getFlag());
        problem.setDifficulty(request.getDifficulty());
        problem.setPortNumber(request.getPortNumber());
        problem.setDockerfileLink(request.getDockerfileLink());
        problem.setUpdatedAt(LocalDateTime.now());

        ContestProblem newProblem = contestProblemRepository.save(problem);
        handleFileUpload(file, newProblem);
        return convertToDto(newProblem, newProblem.getContest().getTitle(), null);
    }

    @Transactional
    public void deleteProblem(Long problemId) {
        if (!contestProblemRepository.existsById(problemId)) {
            throw new SolvedException(ContestProblemErrorCode.CONTEST_PROBLEM_NOT_FOUND);
        }
        contestProblemRepository.deleteById(problemId);
    }

    @Transactional
    public void uploadFile(Long problemId, Long userId, MultipartFile file) {
        ContestProblem problem = contestProblemRepository.findById(problemId)
                .orElseThrow(() -> new SolvedException(ContestProblemErrorCode.CONTEST_PROBLEM_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SolvedException(UserErrorCode.USER_NOT_FOUND));

        checkContestOrganizerAuthorizationOrThrow(user, problem.getContest());
        handleFileUpload(file, problem);
    }

    private void handleFileUpload(MultipartFile file, ContestProblem problem) {
        if (file != null && !file.isEmpty()) {
            if (problem.getProblemFile() != null) {
                fileService.deleteFile(problem.getProblemFile());
            }

            FileInfo fileInfo = fileService.uploadFile(problem.getId(), file);
            problem.setProblemFile(fileInfo.fileName());
            problem.setProblemFileSize(fileInfo.fileSize());

            contestProblemRepository.save(problem);
            log.info("ContestProblem {} file uploaded: {}", problem.getId(), fileInfo.fileName());
        }
    }

    private void checkContestOrganizerAuthorizationOrThrow(User user, Contest contest) {
        if (contest.getOrganizers() == null || contest.getOrganizers().isEmpty() || !contest.getOrganizers().contains(user)) {
            throw new SolvedException(ContestErrorCode.NOT_ELIGIBLE_AFFILIATION);
        }
    }

    @Transactional
    public void lockProblem(Long problemId) {
        ContestProblem problem = contestProblemRepository.findById(problemId)
                .orElseThrow(() -> new SolvedException(ContestProblemErrorCode.CONTEST_PROBLEM_NOT_FOUND));

        if (problem.isLocked()) {
            throw new SolvedException(ContestProblemErrorCode.PROBLEM_ALREADY_LOCKED);
        }
        problem.setLocked(true);
        contestProblemRepository.save(problem);
    }


    @Transactional
    public void unlockProblem(Long problemId) {
        ContestProblem problem = contestProblemRepository.findById(problemId)
                .orElseThrow(() -> new SolvedException(ContestProblemErrorCode.CONTEST_PROBLEM_NOT_FOUND));

        if (!problem.isLocked()) {
            throw new SolvedException(ContestProblemErrorCode.PROBLEM_NOT_LOCKED);
        }
        problem.setLocked(false);
        contestProblemRepository.save(problem);
    }

    @Transactional(readOnly = true)
    public List<ContestProblemDto> getProblems(Long contestId, WargameKind kind, String searchTerm, Long userId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND));

        List<ContestProblem> problems;
        if (searchTerm != null && !searchTerm.isBlank()) {
            problems = contestProblemRepository.findByContestAndTitleContainingIgnoreCase(contest, searchTerm);
        } else if (kind != null) {
            problems = contestProblemRepository.findByContestAndKind(contest, kind);
        } else {
            problems = contestProblemRepository.findByContest(contest);
        }

        Team team = teamRepository.findByContestIdAndMembers_Id(contestId, userId).orElse(null);
        Long teamId = (team != null) ? team.getId() : null;

        return problems.stream()
                .map(problem -> convertToDto(problem, contest.getTitle(), teamId))
                .toList();
    }

    @Transactional(readOnly = true)
    public ContestProblemDto getProblemDetail(Long contestId, Long problemId, Long userId) {
        if (!contestRepository.existsById(contestId)) {
            throw new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND);
        }

        ContestProblem problem = contestProblemRepository.findByContest_IdAndId(contestId, problemId)
                .orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        Team team = teamRepository.findByContestIdAndMembers_Id(contestId, userId).orElse(null);
        Long teamId = (team != null) ? team.getId() : null;

        return convertToDto(problem, problem.getContest().getTitle(), teamId);
    }

    @Transactional(readOnly = true)
    public ContestProblemForOrganizerDto getProblemDetailForOrganizer(Long contestId, Long problemId, Long userId) {
        if (!contestRepository.isUserOrganizer(contestId, userId)) {
            throw new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND);
        }

        ContestProblem problem = contestProblemRepository.findByContest_IdAndId(contestId, problemId)
                .orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        return convertToForOrganizerDto(problem);
    }


    public Resource downloadProblemFile(Long problemId) {
        try {
            Problem problem = contestProblemRepository.findById(problemId)
                    .orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "problemId: " + problemId));

            if (!(problem instanceof ContestProblem contestProblem)) {
                throw new SolvedException(ProblemErrorCode.INVALID_PROBLEM_OPERATION, "only supported for contestProblem");
            }

            if (contestProblem.getProblemFile() == null) {
                throw new SolvedException(ProblemErrorCode.FILE_NOT_FOUND, "problemId : " + problemId);
            }

            Resource resource = fileService.downloadFile(contestProblem.getProblemFile());
            if (!resource.exists()) {
                throw new SolvedException(ProblemErrorCode.FILE_NOT_FOUND, contestProblem.getProblemFile());
            }

            return resource;
        } catch (Exception e) {
            throw new SolvedException(ProblemErrorCode.FILE_UPLOAD_FAILED, "downloadProblemFile failed: " + e.getMessage());
        }
    }

    @Transactional
    public boolean solveProblem(Long userId, Long contestId, Long problemId, String flag) {
        ContestProblem problem = contestProblemRepository.findById(problemId)
                .orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        if (problem.getFlag() == null || flag == null) {
            return false;
        }

        Team team = teamRepository.findByContestIdAndMembers_Id(contestId, userId)
                .orElseThrow(() -> new SolvedException(TeamErrorCode.TEAM_NOT_FOUND));

        boolean alreadySolved = contestSolvedRepository.existsByContest_IdAndTeam_IdAndSolvedProblem_Id(contestId, team.getId(), problemId);
        boolean isCorrect = problem.getFlag().equals(flag);

        if (isCorrect && !alreadySolved) {
            recordSolve(contestId, team.getId(), problemId, userId);
            int problemPoints = problem.getPoints();
            team.setPoints(team.getPoints() + problemPoints);
            teamRepository.save(team);

            problem.setCorrectCount(problem.getCorrectCount() + 1);
        }
        problem.setEntireCount(problem.getEntireCount() + 1);
        contestProblemRepository.save(problem);

        return isCorrect;
    }

    @Transactional
    public void recordSolve(Long contestId, Long teamId, Long problemId, Long userId) {
        if (contestSolvedRepository.existsByContest_IdAndTeam_IdAndSolvedProblem_Id(contestId, teamId, problemId)) {
            return;
        }

        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(TeamErrorCode.CONTEST_NOT_FOUND));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new SolvedException(TeamErrorCode.TEAM_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SolvedException(TeamErrorCode.USER_NOT_FOUND));
        ContestProblem problem = contestProblemRepository.findById(problemId)
                .orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        ContestSolved solved = new ContestSolved();
        solved.setContest(contest);
        solved.setTeam(team);
        solved.setSolvedUser(user);
        solved.setSolvedProblem(problem);
        solved.setSolvedTime(LocalDateTime.now());

        contestSolvedRepository.save(solved);
    }

    // 문제 수정하고 10분 이내는 isNew=true
    private ContestProblemDto convertToDto(ContestProblem problem, String contestName, Long teamId) {
        boolean solved = false;

        if (teamId != null) {
            solved = contestSolvedRepository.existsByContest_IdAndTeam_IdAndSolvedProblem_Id(
                    problem.getContest().getId(), teamId, problem.getId());
        }

        boolean isNew = false;
        LocalDateTime now = LocalDateTime.now();
        if (problem.getUpdatedAt() != null && problem.getUpdatedAt().isAfter(now.minusMinutes(10))) {
            isNew = true;
        }

        return ContestProblemDto.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .detail(problem.getDetail())
                .kind(problem.getKind())
                .creator(UserDto.from(problem.getCreator(), null))
                .difficulty(problem.getDifficulty())
                .tags(problem.getTags())
                .points(problem.getPoints())
                .problemFile(problem.getProblemFile())
                .hasContainer(problem.getDockerfileLink() != null || problem.getProblemFile() != null)
                .isLocked(problem.isLocked())
                .source(contestName)
                .isNew(isNew)
                .solved(solved)
                .entireCount(problem.getEntireCount())
                .correctCount(problem.getCorrectCount())
                .createdAt(problem.getCreatedAt())
                .updatedAt(problem.getUpdatedAt())
                .build();
    }


    private ContestProblemForOrganizerDto convertToForOrganizerDto(ContestProblem problem) {
        return ContestProblemForOrganizerDto.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .detail(problem.getDetail())
                .kind(problem.getKind())
                .creator(UserDto.from(problem.getCreator(), null))
                .difficulty(problem.getDifficulty())
                .tags(problem.getTags())
                .points(problem.getPoints())
                .flag(problem.getFlag())
                .dockerfileLink(problem.getDockerfileLink())
                .portNumber(problem.getPortNumber())
                .problemFile(problem.getProblemFile())
                .hasContainer(problem.getDockerfileLink() != null || problem.getProblemFile() != null)
                .isLocked(problem.isLocked())
                .entireCount(problem.getEntireCount())
                .correctCount(problem.getCorrectCount())
                .createdAt(problem.getCreatedAt())
                .updatedAt(problem.getUpdatedAt())
                .build();
    }

}
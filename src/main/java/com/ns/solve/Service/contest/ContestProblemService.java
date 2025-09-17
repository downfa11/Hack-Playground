package com.ns.solve.service.contest;

import com.ns.solve.domain.dto.contest.ModifyContestProblemRequest;
import com.ns.solve.domain.dto.contest.RegisterContestProblemRequest;
import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.contest.ContestProblem;
import com.ns.solve.domain.vo.WargameKind;
import com.ns.solve.repository.contest.ContestProblemRepository;
import com.ns.solve.repository.contest.ContestRepository;
import com.ns.solve.utils.exception.ErrorCode.ContestErrorCode;
import com.ns.solve.utils.exception.ErrorCode.ContestProblemErrorCode;
import com.ns.solve.utils.exception.SolvedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContestProblemService {

    private final ContestProblemRepository contestProblemRepository;
    private final ContestRepository contestRepository;

    @Transactional
    public ContestProblem createProblem(Long contestId, RegisterContestProblemRequest request) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND));

        if (contestProblemRepository.existsByContestAndTitle(contest, request.getTitle())) {
            throw new SolvedException(ContestProblemErrorCode.DUPLICATE_PROBLEM_TITLE);
        }

        ContestProblem newProblem = ContestProblem.builder()
                .title(request.getTitle())
                .type(request.getType())
                .detail(request.getDetail())
                .tags(request.getTags())
                .points(request.getPoints())
                .kind(request.getKind())
                .flag(request.getFlag())
                .dockerfileLink(request.getDockerfileLink())
                .problemFile(request.getProblemFile())
                .kind(request.getKind())
                .build();

        newProblem.setContest(contest);
        return contestProblemRepository.save(newProblem);
    }

    @Transactional
    public ContestProblem updateProblem(Long problemId, ModifyContestProblemRequest request) {
        ContestProblem problem = contestProblemRepository.findById(problemId)
                .orElseThrow(() -> new SolvedException(ContestProblemErrorCode.CONTEST_PROBLEM_NOT_FOUND));

        if (!problem.getTitle().equals(request.getTitle()) && contestProblemRepository.existsByContestAndTitle(problem.getContest(), request.getTitle())) {
            throw new SolvedException(ContestProblemErrorCode.DUPLICATE_PROBLEM_TITLE);
        }

        problem.setTitle(request.getTitle());
        problem.setDetail(request.getDetail());
        problem.setPoints(request.getPoints());
        problem.setKind(request.getKind());
        problem.setTags(request.getTags());
        problem.setFlag(request.getFlag());
        problem.setDockerfileLink(request.getDockerfileLink());
        problem.setProblemFile(request.getProblemFile());
        problem.setKind(request.getKind());

        return contestProblemRepository.save(problem);
    }

    @Transactional
    public void deleteProblem(Long problemId) {
        if (!contestProblemRepository.existsById(problemId)) {
            throw new SolvedException(ContestProblemErrorCode.CONTEST_PROBLEM_NOT_FOUND);
        }
        contestProblemRepository.deleteById(problemId);
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
    public List<ContestProblem> getProblems(Long contestId, WargameKind kind, String searchTerm) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND));

        if (searchTerm != null && !searchTerm.isBlank()) {
            return contestProblemRepository.findByContestAndTitleContainingIgnoreCase(contest, searchTerm);
        }

        if (kind != null) {
            return contestProblemRepository.findByContestAndKind(contest, kind);
        }

        return contestProblemRepository.findByContest(contest);
    }
}
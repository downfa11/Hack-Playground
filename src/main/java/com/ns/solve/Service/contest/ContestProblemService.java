package com.ns.solve.service.contest;

import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.contest.ContestProblem;
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

    @Transactional(readOnly = true)
    public List<ContestProblem> getProblems(Long contestId, String category, String searchTerm) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND));

        if (searchTerm != null && !searchTerm.isBlank()) {
            return contestProblemRepository.findByContestAndTitleContainingIgnoreCase(contest, searchTerm);
        }

        if (category != null && !category.isBlank()) {
            return contestProblemRepository.findByContestAndCategory(contest, category);
        }

        return contestProblemRepository.findByContest(contest);
    }

//    @Transactional
//    public ContestProblem createProblem(Long contestId, ContestProblem problem) {
//        Contest contest = contestRepository.findById(contestId)
//                .orElseThrow(() -> new SolvedException(ContestErrorCode.CONTEST_NOT_FOUND));
//
//        if (contestProblemRepository.existsByContestAndTitle(contest, problem.getTitle())) {
//            throw new SolvedException(ContestProblemErrorCode.DUPLICATE_PROBLEM_TITLE);
//        }
//
//        problem.setContest(contest);
//        return contestProblemRepository.save(problem);
//    }
//
//    @Transactional
//    public ContestProblem updateProblem(Long problemId, ContestProblem updatedProblemData) {
//        ContestProblem problem = contestProblemRepository.findById(problemId)
//                .orElseThrow(() -> new SolvedException(ContestProblemErrorCode.CONTEST_PROBLEM_NOT_FOUND));
//
//        if (!problem.getTitle().equals(updatedProblemData.getTitle()) && contestProblemRepository.existsByContestAndTitle(problem.getContest(), updatedProblemData.getTitle())) {
//            throw new SolvedException(ContestProblemErrorCode.DUPLICATE_PROBLEM_TITLE);
//        }
//
//        problem.setTitle(updatedProblemData.getTitle());
//        problem.setDescription(updatedProblemData.getDescription());
//        problem.setScore(updatedProblemData.getScore());
//        problem.setCategory(updatedProblemData.getCategory());
//
//        return contestProblemRepository.save(problem);
//    }

    @Transactional
    public void deleteProblem(Long problemId) {
        if (!contestProblemRepository.existsById(problemId)) {
            throw new SolvedException(ContestProblemErrorCode.CONTEST_PROBLEM_NOT_FOUND);
        }
        contestProblemRepository.deleteById(problemId);
    }
}
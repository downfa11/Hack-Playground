package com.ns.solve.repository.problem;

import com.ns.solve.domain.dto.problem.ProblemSummary;
import com.ns.solve.domain.problem.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ProblemCustomRepository {
    Page<Problem> findProblemsByStatusPending(PageRequest pageRequest);
    Page<ProblemSummary> findProblemsByStatusAndTypeSortedById(String type, boolean desc, PageRequest pageRequest);
    Page<ProblemSummary> findProblemsByStatusAndTypeSortedByUpdatedAt(String type, boolean desc, PageRequest pageRequest);
    Page<ProblemSummary> findProblemsByStatusAndTypeSortedByCorrectRate(String type, boolean desc, PageRequest pageRequest);
    Boolean matchFlagToProblems(Long problemId, String attemptedFlag);
}

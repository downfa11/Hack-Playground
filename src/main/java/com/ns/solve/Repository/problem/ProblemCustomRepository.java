package com.ns.solve.repository.problem;

import com.ns.solve.domain.dto.problem.ProblemSummary;
import com.ns.solve.domain.problem.Problem;
import com.ns.solve.domain.problem.ProblemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ProblemCustomRepository {
    Page<Problem> findProblemsByStatusPending(PageRequest pageRequest);
    Page<ProblemSummary> findProblemsByStatusAndTypeSortedById(ProblemType type, String kind, boolean desc, PageRequest pageRequest);
    Page<ProblemSummary> findProblemsByStatusAndTypeSortedByUpdatedAt(ProblemType type, String kind, boolean desc, PageRequest pageRequest);
    Page<ProblemSummary> findProblemsByStatusAndTypeSortedByCorrectRate(ProblemType type, String kind, boolean desc, PageRequest pageRequest);
    Boolean matchFlagToProblems(Long problemId, String attemptedFlag);
}

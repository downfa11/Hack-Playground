package com.ns.solve.repository.problem;

import com.ns.solve.domain.dto.problem.ProblemSummary;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.domain.entity.problem.ProblemType;
import com.ns.solve.domain.entity.problem.WargameKind;
import com.ns.solve.domain.entity.problem.WargameProblem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ProblemCustomRepository {
    Page<ProblemSummary> searchKeywordInTitle(ProblemType type, WargameKind kind, String keyword, Pageable pageable);


    Page<Problem> findProblemsByStatusPending(PageRequest pageRequest);
    Page<ProblemSummary> findProblemsByStatusAndTypeSortedById(ProblemType type, WargameKind kind, boolean desc, PageRequest pageRequest);
    Page<ProblemSummary> findProblemsByStatusAndTypeSortedByUpdatedAt(ProblemType type, WargameKind kind, boolean desc, PageRequest pageRequest);
    Page<ProblemSummary> findProblemsByStatusAndTypeSortedByCorrectRate(ProblemType type, WargameKind kind, boolean desc, PageRequest pageRequest);
    Boolean matchFlagToWargameProblem(Long problemId, String attemptedFlag);

    List<WargameProblem> findByTypeWargame(ProblemType wargameType);
    Problem findProblemWithLock(Long problemId);

    long countCheckedProblems();
    long countNewProblems(LocalDateTime now);

}

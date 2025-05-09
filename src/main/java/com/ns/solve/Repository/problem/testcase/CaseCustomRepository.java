package com.ns.solve.repository.problem.testcase;

import com.ns.solve.domain.entity.problem.Case;
import java.util.Optional;

public interface CaseCustomRepository {
    Optional<Case> findByCaseId(Long caseId);
}

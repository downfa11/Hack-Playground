package com.ns.solve.repository.problem.testcase;

import com.ns.solve.domain.problem.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long>, CaseCustomRepository {
}

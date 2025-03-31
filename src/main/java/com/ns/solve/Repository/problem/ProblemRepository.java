package com.ns.solve.repository.problem;

import com.ns.solve.domain.problem.Problem;
import com.ns.solve.domain.problem.ProblemType;
import com.ns.solve.domain.problem.WargameProblem;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long>, ProblemCustomRepository {
    @Query("SELECT p FROM Problem p WHERE p.type = :wargameType")
    List<WargameProblem> findByTypeWargame(ProblemType wargameType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p " +
            "FROM Problem p " +
            "WHERE p.id = :problemId")
    Problem findProblemWithLock(Long problemId);
}

package com.ns.solve.repository.problem;

import com.ns.solve.domain.entity.problem.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long>, ProblemCustomRepository {

    @Query("SELECT p.title FROM Problem p WHERE p.id = :problem_id")
    String findTitleByProblemId(@Param("problem_id") Long problemId);
}

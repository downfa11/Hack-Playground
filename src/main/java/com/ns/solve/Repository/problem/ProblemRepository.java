package com.ns.solve.repository.problem;

import com.ns.solve.domain.entity.problem.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long>, ProblemCustomRepository {

    @Query("SELECT p.title FROM Problem p WHERE p.id = :problem_id")
    String findTitleByProblemId(@Param("problem_id") Long problemId);

    Long countByIsChecked(boolean isChecked);

    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT wp.kind, COUNT(wp) FROM WargameProblem wp GROUP BY wp.kind")
    List<Object[]> countWargameProblemsGroupedByKind(); // wargame만 한정


    @Query("SELECT p FROM Problem p WHERE p.creator.id = :userId")
    Page<Problem> findByUserId(@Param("userId") Long userId, Pageable pageable);
}

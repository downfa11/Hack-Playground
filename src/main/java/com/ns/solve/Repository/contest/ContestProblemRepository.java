package com.ns.solve.repository.contest;

import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.contest.ContestProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContestProblemRepository extends JpaRepository<ContestProblem, Long> {
    List<ContestProblem> findByContest(Contest contest);
    List<ContestProblem> findByContestAndCategory(@Param("contest") Contest contest, @Param("category") String category);
    List<ContestProblem> findByContestAndTitleContainingIgnoreCase(Contest contest, String title);
    boolean existsByContestAndTitle(Contest contest, String title);
}
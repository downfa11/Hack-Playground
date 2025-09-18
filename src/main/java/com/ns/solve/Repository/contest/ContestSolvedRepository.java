package com.ns.solve.repository.contest;

import com.ns.solve.domain.entity.contest.ContestSolved;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContestSolvedRepository extends JpaRepository<ContestSolved, Long> {
    List<ContestSolved> findByContest_Id(Long contestId);
    Optional<ContestSolved> findByContest_IdAndTeam_IdAndSolvedProblem_Id(Long contestId, Long teamId, Long problemId);
}

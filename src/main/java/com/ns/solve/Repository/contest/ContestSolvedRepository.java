package com.ns.solve.repository.contest;

import com.ns.solve.domain.entity.contest.ContestSolved;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContestSolvedRepository extends JpaRepository<ContestSolved, Long> {
    List<ContestSolved> findByContest_Id(Long contestId);
    boolean existsByContest_IdAndTeam_IdAndSolvedProblem_Id(Long contestId, Long teamId, Long problemId);

    @Query("SELECT cs FROM ContestSolved cs " +
            "JOIN FETCH cs.solvedUser su " +
            "JOIN FETCH cs.solvedProblem sp " +
            "WHERE cs.contest.id = :contestId")
    List<ContestSolved> findByContestIdWithUserAndProblem(@Param("contestId") Long contestId);

    @Query("SELECT cs FROM ContestSolved cs " +
            "JOIN FETCH cs.team t " +
            "JOIN FETCH cs.solvedProblem sp " +
            "WHERE cs.contest.id = :contestId " +
            "ORDER BY cs.solvedTime ASC")
    List<ContestSolved> findByContestIdWithTeamAndProblemOrderBySolvedTime(@Param("contestId") Long contestId);
}

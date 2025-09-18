package com.ns.solve.repository.contest;

import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.contest.ContestProblem;
import com.ns.solve.domain.vo.WargameKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContestProblemRepository extends JpaRepository<ContestProblem, Long> {
    Optional<ContestProblem> findByContestIdAndProblemId(Long contestId, Long problemId);
    List<ContestProblem> findByContest(Contest contest);
    List<ContestProblem> findByContestAndKind(Contest contest, WargameKind kind);
    List<ContestProblem> findByContestAndTitleContainingIgnoreCase(Contest contest, String title);
    boolean existsByContestAndTitle(Contest contest, String title);
}
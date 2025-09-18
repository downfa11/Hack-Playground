package com.ns.solve.repository.contest;

import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.contest.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByContestAndName(Contest contest, String name);

    List<Team> findByContestId(Long contestId);

    Optional<Team> findByContestIdAndUserId(Long contestId, Long userId);
    Optional<Team> findByContestIdAndMembers_Id(Long contestId, Long memberId);
}
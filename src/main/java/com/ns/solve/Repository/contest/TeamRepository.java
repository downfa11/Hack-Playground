package com.ns.solve.repository.contest;

import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.contest.Team;
import com.ns.solve.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByContestAndName(Contest contest, String name);
    boolean existsByContestIdAndName(Long contestId, String name);
    int countByContestId(Long contestId);
    List<Team> findByContestId(Long contestId);
    Optional<Team> findByContestIdAndMembers_Id(Long contestId, Long memberId);

    Optional<Team> findByContestIdAndMembersContains(Long contestId, User member);
}
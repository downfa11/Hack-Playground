package com.ns.solve.repository.contest;

import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.contest.Team;
import com.ns.solve.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByContestAndName(Contest contest, String name);
    boolean existsByContestIdAndName(Long contestId, String name);
    int countByContestId(Long contestId);
    List<Team> findByContestId(Long contestId);
    Optional<Team> findByContestIdAndMembers_Id(Long contestId, Long memberId);
    Optional<Team> findByContestIdAndName(Long contestId, String name);
    Optional<Team> findByContestIdAndMembersContains(Long contestId, User member);

    @Query("SELECT DISTINCT t FROM Team t " +
            "LEFT JOIN FETCH t.members m " +
            "WHERE t.contest.id = :contestId " +
            "ORDER BY t.points DESC")
    List<Team> findByContestIdWithMembers(@Param("contestId") Long contestId);

    @Query("SELECT t FROM Team t " +
            "LEFT JOIN FETCH t.members m " +
            "WHERE t.contest.id = :contestId AND :user MEMBER OF t.members")
    Optional<Team> findByContestIdAndMembersContainsWithMembers(@Param("contestId") Long contestId, @Param("user") User user);
}
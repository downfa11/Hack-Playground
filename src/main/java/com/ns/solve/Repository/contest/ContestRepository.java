package com.ns.solve.repository.contest;

import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.user.User;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {

    @EntityGraph(attributePaths = {"organizers"})
    List<Contest> findByTitleContainingIgnoreCase(String searchTerm); // 제목 포함한 검색
    @EntityGraph(attributePaths = {"organizers"})
    List<Contest> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime now); // 예정된 대회 목록

    // 진행 중인 대회 목록
    List<Contest> findByStartTimeBeforeAndEndTimeAfterOrderByEndTimeAsc(LocalDateTime startTime, LocalDateTime endTime);

    List<Contest> findByEndTimeBeforeOrderByEndTimeDesc(LocalDateTime now); // 종료된 대회 목록

    boolean existsByTitleAndStartTime(String title, LocalDateTime startTime);

    // todo. 특정 사용자가 참가한 대회 목록
    @Query("SELECT c FROM Contest c JOIN c.participants p WHERE p = :user")
    List<Contest> findContestsByParticipant(@Param("user") User user);


    // 현재 진행중인 대회 수 계산
    int countByStartTimeBeforeAndEndTimeAfter(LocalDateTime startTime, LocalDateTime endTime);


    int countByStartTimeAfter(LocalDateTime date); // 월별 대회 수 계산

    // 월별 참가자 수
    @Query("SELECT COUNT(DISTINCT p.id) FROM Contest c JOIN c.participants p WHERE c.startTime >= :startOfMonth")
    int countDistinctParticipantsByStartTimeAfter(@Param("startOfMonth") LocalDateTime startOfMonth);


    // 해당 대회의 운영자인지 userId로 검색
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Contest c JOIN c.organizers u " +
            "WHERE c.id = :contestId AND u.id = :userId")
    boolean isUserOrganizer(@Param("contestId") Long contestId, @Param("userId") Long userId);

    @EntityGraph(attributePaths = {"organizers", "participants", "affiliations", "affiliationTypes", "problems", "prizes"})
    Optional<Contest> findById(Long id);
}
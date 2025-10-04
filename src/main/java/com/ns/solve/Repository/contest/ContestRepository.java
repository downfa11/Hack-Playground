package com.ns.solve.repository.contest;

import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {

    List<Contest> findByTitleContainingIgnoreCase(String searchTerm); // 제목 포함한 검색
    List<Contest> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime now); // 예정된 대회 목록

    // 진행 중인 대회 목록
    List<Contest> findByStartTimeBeforeAndEndTimeAfterOrderByEndTimeAsc(LocalDateTime startTime, LocalDateTime endTime);

    List<Contest> findByEndTimeBeforeOrderByEndTimeDesc(LocalDateTime now); // 종료된 대회 목록

    // 특정 사용자가 참가한 대회 목록
    @Query("SELECT c FROM Contest c JOIN c.participants p WHERE p = :user")
    List<Contest> findContestsByParticipant(@Param("user") User user);


    // 현재 진행중인 대회 수 계산
    int countByStartTimeBeforeAndEndTimeAfter(LocalDateTime startTime, LocalDateTime endTime);


    int countByStartTimeAfter(LocalDateTime date); // 월별 대회 수 계산

    // 월별 참가자 수
    @Query("SELECT COUNT(DISTINCT p) FROM Contest c JOIN c.participants p WHERE c.startTime >= :startOfMonth")
    int countDistinctParticipantsByStartTimeAfter(@Param("startOfMonth") LocalDateTime startOfMonth);
}
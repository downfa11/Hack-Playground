package com.ns.solve.repository.contest;

import com.ns.solve.domain.entity.contest.Contest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {

    // 대회 제목을 기준으로 검색
    List<Contest> findByTitleContainingIgnoreCase(String searchTerm);

    // 예정된 대회 목록 조회 (시작 시간 이후)
    List<Contest> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime now);

    // 진행 중인 대회 목록 조회 (시작 시간 이전 && 종료 시간 이후)
    List<Contest> findByStartTimeBeforeAndEndTimeAfterOrderByEndTimeAsc(LocalDateTime now, LocalDateTime now2);

    // 종료된 대회 목록 조회 (종료 시간 이전)
    List<Contest> findByEndTimeBeforeOrderByEndTimeDesc(LocalDateTime now);
}
package com.ns.solve.repository.contest;

import com.ns.solve.domain.entity.contest.Prize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PrizeRepository extends JpaRepository<Prize, Long> {

    // 월별 수상자 수
    @Query("SELECT COUNT(DISTINCT w) FROM Prize p JOIN p.winners w WHERE p.contest.endTime >= :startOfMonth")
    int countDistinctWinnersByContestEndTimeAfter(@Param("startOfMonth") LocalDateTime startOfMonth);

}
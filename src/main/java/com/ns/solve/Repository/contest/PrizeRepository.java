package com.ns.solve.repository.contest;

import com.ns.solve.domain.entity.contest.Prize;
import com.ns.solve.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PrizeRepository extends JpaRepository<Prize, Long> {

    // 월별 수상자 수
    @Query("SELECT COUNT(DISTINCT w) FROM Prize p JOIN p.winners w WHERE p.contest.endTime >= :startOfMonth")
    int countDistinctWinnersByContestEndTimeAfter(@Param("startOfMonth") LocalDateTime startOfMonth);

    @Query("SELECT p FROM Prize p " +
            "JOIN FETCH p.contest c " +
            "LEFT JOIN FETCH p.winners w " +
            "WHERE :user MEMBER OF p.winners")
    List<Prize> findPrizesWithContestAndWinnersByUser(@Param("user") User user);

    @Query("select case when count(p) > 0 then true else false end " +
            "from Prize p join p.winners w where p.contest.id = :contestId")
    boolean existsWinnersByContestId(@Param("contestId") Long contestId);

    @Query("""
    select distinct p
    from Prize p
    join fetch p.contest c
    left join fetch p.winners w
    where c.id = :contestId
    """)
    List<Prize> findByContestIdWithWinners(@Param("contestId") Long contestId);

}
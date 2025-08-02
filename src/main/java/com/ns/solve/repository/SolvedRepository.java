package com.ns.solve.repository;

import com.ns.solve.domain.dto.user.UserFirstBloodDto;
import com.ns.solve.domain.entity.Solved;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SolvedRepository extends JpaRepository<Solved, Long> {
    // problemId를 통해서 가장 처음 해당 문제를 푼 사람을 확인하는 메서드
    @Query("""
    SELECT new com.ns.solve.domain.dto.user.UserFirstBloodDto(u.id, u.nickname, u.role, MIN(s.solvedTime))
    FROM Solved s
    JOIN s.solvedUser u
    WHERE s.solvedProblem.id = :problemId AND s.solve = true
    GROUP BY u.id, u.nickname, u.role
    ORDER BY MIN(s.solvedTime) ASC""")
    List<UserFirstBloodDto> findFirstBloodByProblemId(@Param("problemId") Long problemId, Pageable pageable);


    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM Solved s WHERE s.solvedUser.id = :userId AND s.solvedProblem.id = :problemId AND s.solve = true")
    Boolean existsSolvedProblem(@Param("userId") Long userId, @Param("problemId") Long problemId);

    @Query("SELECT DISTINCT s.solvedProblem.title FROM Solved s WHERE s.solvedUser.id = :userId AND s.solve = true")
    List<String> findSolvedProblemTitlesByUserId(@Param("userId") Long userId);


    // DISTINCT: 같은 문제를 여러번 풀어도 하나 푼 것으로 처리한다.
    @Query("""
    SELECT COUNT(DISTINCT s.solvedProblem.id)
    FROM Solved s
    WHERE s.solvedTime BETWEEN :from AND :to
    """)
    Long countRecentlyTriedProblems(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);


    Long countBySolvedTimeBetween(LocalDateTime start, LocalDateTime end);
}

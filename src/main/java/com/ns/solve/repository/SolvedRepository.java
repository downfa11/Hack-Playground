package com.ns.solve.repository;

import com.ns.solve.domain.Solved;
import com.ns.solve.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SolvedRepository extends JpaRepository<Solved, Long> {
    // problemId를 통해서 가장 처음 해당 문제를 푼 사람을 확인하는 메서드
    @Query("SELECT u FROM Solved s JOIN s.solvedUser u WHERE s.solvedProblem.id = :problemId and s.solve=true ORDER BY s.solvedTime ASC")
    Page<User> findFirstUserToSolveProblem(@Param("problemId") Long problemId, Pageable pageable);

    // JPA의 N+1 문제는 생기지 않지만 User 엔티티까지 한번의 쿼리로 가져오도록 설계


    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM Solved s WHERE s.solvedUser.id = :userId AND s.solvedProblem.id = :problemId AND s.solve = true")
    Boolean existsSolvedProblem(@Param("userId") Long userId, @Param("problemId") Long problemId);
}

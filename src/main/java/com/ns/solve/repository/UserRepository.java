package com.ns.solve.repository;

import com.ns.solve.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 전체 문제를 푼 개수에 따라서 위에서부터 정렬하여 Pagenation 끊어서 조회하는 메서드
    // 유형별 문제를 푼 개수에 따라서 위에서부터 정렬하여 Pagenation 끊어서 조회하는 메서드
    User findByAccount(String account);
    User findByNickname(String nickname);

    Boolean existsByNickname(String nickname);
    Boolean existsByAccount(String account);

    Page<User> findAllByScoreGreaterThanOrderByScoreDesc(long score, Pageable pageable);


    @Query("SELECT u FROM User u WHERE key(u.fieldScores) = :type AND value(u.fieldScores) > 0 ORDER BY value(u.fieldScores) DESC")
    Page<User> findUsersByFieldScore(@Param("type") String type, Pageable pageable);
}

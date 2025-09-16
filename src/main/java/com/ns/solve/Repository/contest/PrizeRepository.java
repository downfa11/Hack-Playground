package com.ns.solve.repository.contest;

import com.ns.solve.domain.entity.contest.Prize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrizeRepository extends JpaRepository<Prize, Long> {
}
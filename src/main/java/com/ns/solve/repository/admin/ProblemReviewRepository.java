package com.ns.solve.repository.admin;

import com.ns.solve.domain.entity.admin.ProblemReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProblemReviewRepository extends JpaRepository<ProblemReview, Long> {

    List<ProblemReview> findProblemReviewsByReviewerId(Long reviewerId);
    @Query("SELECT pr FROM ProblemReview pr JOIN FETCH pr.reviewer WHERE pr.problem.id = :problemId ORDER BY pr.createdAt DESC")
    List<ProblemReview> findProblemReviewsByProblemId(Long problemId);
}

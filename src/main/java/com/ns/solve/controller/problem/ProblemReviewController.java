package com.ns.solve.controller.problem;

import com.ns.solve.domain.entity.admin.ProblemReview;
import com.ns.solve.service.problem.ProblemReviewService;
import com.ns.solve.utils.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problem/review")
public class ProblemReviewController {
    private final ProblemReviewService problemReviewService;

    @GetMapping("/{problemId}")
    public List<ProblemReview> getProblemReviewsByProblem(@PathVariable Long problemId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        return problemReviewService.getProblemReviewsByProblem(problemId, userId);
    }
}

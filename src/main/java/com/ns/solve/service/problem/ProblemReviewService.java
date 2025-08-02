package com.ns.solve.service.problem;

import com.ns.solve.domain.entity.admin.ProblemReview;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.domain.entity.user.User;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.repository.admin.ProblemReviewRepository;
import com.ns.solve.repository.problem.ProblemRepository;
import com.ns.solve.utils.exception.ErrorCode.ProblemErrorCode;
import com.ns.solve.utils.exception.SolvedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemReviewService {
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final ProblemReviewRepository problemReviewRepository;

    @Transactional(readOnly = true)
    public List<ProblemReview> getProblemReviewsByReviewer(Long reviewerId) {
        return problemReviewRepository.findProblemReviewsByReviewerId(reviewerId);
    }

    @Transactional(readOnly = true)
    public List<ProblemReview> getProblemReviewsByProblem(Long problemId, Long userId) {
        Problem existingProblem = problemRepository.findById(problemId)
                .orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "problemId: " + problemId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "userId: " + userId));

        checkAuthorizationOrThrow(user, existingProblem);
        return problemReviewRepository.findProblemReviewsByProblemId(problemId);
    }

    private void checkAuthorizationOrThrow(User user, Problem problem) {
        if (!user.isMemberAbove() && !problem.getCreator().equals(user)) {
            throw new SolvedException(ProblemErrorCode.ACCESS_DENIED);
        }
    }
}
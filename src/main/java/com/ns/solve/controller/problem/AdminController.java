package com.ns.solve.controller.problem;

import com.ns.solve.domain.dto.MessageEntity;
import com.ns.solve.domain.dto.admin.AdminStatsResponse;
import com.ns.solve.domain.dto.admin.AdminUserResponse;
import com.ns.solve.domain.dto.admin.AdminUserUpdateRequest;
import com.ns.solve.domain.dto.admin.AnalyticsResponse;
import com.ns.solve.domain.dto.problem.ProblemCheckDto;
import com.ns.solve.domain.dto.problem.wargame.WargameProblemDto;
import com.ns.solve.domain.entity.admin.CategoryDistributionResponse;
import com.ns.solve.domain.entity.admin.ProblemReview;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.service.admin.AdminService;
import com.ns.solve.service.problem.ProblemReviewService;
import com.ns.solve.service.problem.ProblemService;
import com.ns.solve.utils.CustomUserDetails;
import com.ns.solve.utils.DummyGenerator;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {
    private final ProblemService problemService;
    private final ProblemReviewService problemReviewService;

    private final AdminService adminService;
    private final DummyGenerator dummyGenerator;


    @Operation(summary="검수를 위해 대기중인 문제 목록 조회", description = "제출된 문제를 검수하기 위해 대기중인 리스트를 조회합니다.")
    @GetMapping("/pending")
    public ResponseEntity<Page<Problem>> getPendingProblems(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<Problem> problems = problemService.getPendingProblems(PageRequest.of(page, size));
        return new ResponseEntity<>(problems, HttpStatus.OK);
    }

    @Operation(summary = "검수 완료", description = "해당 문제의 검수를 완료해서 사용자들에게 보여집니다.")
    @PutMapping("/check/{id}")
    public ResponseEntity<MessageEntity> checkProblem(@PathVariable Long id, @RequestBody ProblemCheckDto problemCheckDto, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long reviewerId = userDetails.getUserId();

        WargameProblemDto checkedProblem = problemService.toggleProblemCheckStatus(reviewerId, id, problemCheckDto);
        return ResponseEntity.ok(new MessageEntity("Problem marked as checked", checkedProblem));
    }

    @Operation(summary = "더미 데이터 생성", description = "테스트를 위한 더미 데이터를 생성합니다.")
    @GetMapping("/generate-dummy")
    public ResponseEntity<MessageEntity> generateDummyData(
            @RequestParam(defaultValue = "1000") int userCount,
            @RequestParam(defaultValue = "2000") int boardCount,
            @RequestParam(defaultValue = "5") int boardCommentCount,
            @RequestParam(defaultValue = "100") int problemCount,
            @RequestParam(defaultValue = "3") int problemCommentCount,
            @RequestParam(defaultValue = "10000") int solvedCount) {

        dummyGenerator.generateDummyData(userCount, boardCount, boardCommentCount, problemCount, problemCommentCount, solvedCount);
        return ResponseEntity.ok(new MessageEntity("Dummy data generated successfully", "succes"));
    }

    @GetMapping("/stats")
    public AdminStatsResponse getAdminStats() {
        return adminService.getStatistics();
    }

    @GetMapping("/users")
    public List<AdminUserResponse> getAllUsers() {
        return adminService.getAllUsers();
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id, @RequestBody AdminUserUpdateRequest request) {
        adminService.updateUser(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // period = daily, monthly, yearly
    @GetMapping("/analytics")
    public List<AnalyticsResponse> getAnalytics(@RequestParam("period") String period) {
        return adminService.getAnalyticsData(period);
    }

    @GetMapping("/distribution")
    public List<CategoryDistributionResponse> getCategoryDistribution() {
        return adminService.getCategoryDistribution();
    }

    @GetMapping("/find/review/{reviewerId}")
    public List<ProblemReview> getProblemReviewsByReviewer(@PathVariable Long reviewerId) {
        return problemReviewService.getProblemReviewsByReviewer(reviewerId);
    }
}

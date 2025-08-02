package com.ns.solve.service.admin;

import com.ns.solve.domain.dto.admin.AdminStatsResponse;
import com.ns.solve.domain.dto.admin.AdminUserResponse;
import com.ns.solve.domain.dto.admin.AdminUserUpdateRequest;
import com.ns.solve.domain.dto.admin.AnalyticsResponse;
import com.ns.solve.domain.entity.problem.WargameKind;
import com.ns.solve.domain.entity.user.Role;
import com.ns.solve.domain.entity.user.User;
import com.ns.solve.domain.entity.admin.CategoryDistributionResponse;
import com.ns.solve.repository.SolvedRepository;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.repository.problem.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class AdminService {
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final SolvedRepository solvedRepository;

    /*===========================Admin===============================*/
    public AdminStatsResponse getStatistics() {
        Long totalUsers = userRepository.count();
        Long pendingProblems = problemRepository.countByIsChecked(false);
        Long totalProblems = problemRepository.countByIsChecked(true);
        Long activeUsers = userRepository.countByLastActivedAfter(LocalDateTime.now().minusHours(24));

        return new AdminStatsResponse(
                totalUsers,
                pendingProblems,
                totalProblems,
                activeUsers
        );
    }

    /*===========================AdminUserList===============================*/
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new AdminUserResponse(
                        user.getId(),
                        user.getNickname(),
                        user.getAccount(),
                        user.getRole().name(),
                        user.getCreated().toString(),
                        user.getLastActived() != null ? user.getLastActived().toString() : "-",
                        user.getScore() != null ? user.getScore() : 0L
                ))
                .collect(Collectors.toList());
    }

    public void updateUser(Long id, AdminUserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        user.setNickname(request.getNickname());
        user.setAccount(request.getAccount());
        user.setRole(Role.valueOf(request.getRole()));

        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /*===========================AdminAnalytics===============================*/

    public List<AnalyticsResponse> getAnalyticsData(String period) {
        if ("daily".equals(period)) {
            return getDailyAnalytics();
        } else if ("monthly".equals(period)) {
            return getMonthlyAnalytics();
        } else if ("yearly".equals(period)) {
            return getYearlyAnalytics();
        } else {
            throw new IllegalArgumentException("Invalid period");
        }
    }

    private List<AnalyticsResponse> getDailyAnalytics() {
        List<AnalyticsResponse> result = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            result.add(new AnalyticsResponse(
                    day.getMonthValue() + "/" + day.getDayOfMonth(),
                    userRepository.countByCreatedBetween(day.atStartOfDay(), day.plusDays(1).atStartOfDay()),
                    problemRepository.countByCreatedAtBetween(day.atStartOfDay(), day.plusDays(1).atStartOfDay()),
                    solvedRepository.countBySolvedTimeBetween(day.atStartOfDay(), day.plusDays(1).atStartOfDay()),
                    userRepository.countActiveUsersBetween(day.atStartOfDay(), day.plusDays(1).atStartOfDay())
            ));
        }
        return result;
    }

    private List<AnalyticsResponse> getMonthlyAnalytics() {
        List<AnalyticsResponse> result = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            LocalDateTime start = month.atDay(1).atStartOfDay();
            LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();
            result.add(new AnalyticsResponse(
                    month.toString(),
                    userRepository.countByCreatedBetween(start, end),
                    problemRepository.countByCreatedAtBetween(start, end),
                    solvedRepository.countBySolvedTimeBetween(start, end),
                    userRepository.countActiveUsersBetween(start, end)
            ));
        }
        return result;
    }

    private List<AnalyticsResponse> getYearlyAnalytics() {
        List<AnalyticsResponse> result = new ArrayList<>();

        for (int i = 4; i >= 0; i--) {
            int year = Year.now().getValue() - i;
            LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
            LocalDateTime end = start.plusYears(1);
            result.add(new AnalyticsResponse(
                    String.valueOf(year),
                    userRepository.countByCreatedBetween(start, end),
                    problemRepository.countByCreatedAtBetween(start, end),
                    solvedRepository.countBySolvedTimeBetween(start, end),
                    userRepository.countActiveUsersBetween(start, end)
            ));
        }
        return result;
    }

    public List<CategoryDistributionResponse> getCategoryDistribution() {
        List<Object[]> result = problemRepository.countWargameProblemsGroupedByKind();

        return result.stream().map(row -> new CategoryDistributionResponse(
                ((WargameKind) row[0]).getTypeName(), ((Number) row[1]).intValue()
        )).collect(Collectors.toList());
    }
}

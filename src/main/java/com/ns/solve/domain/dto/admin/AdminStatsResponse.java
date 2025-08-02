package com.ns.solve.domain.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long pendingProblems;
    private long totalProblems;
    private long activeUsers;
}

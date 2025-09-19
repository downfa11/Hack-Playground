package com.ns.solve.domain.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {
    private String period;
    private Long users;
    private Long problems;
    private Long submissions;
    private Long activeUsers;
}


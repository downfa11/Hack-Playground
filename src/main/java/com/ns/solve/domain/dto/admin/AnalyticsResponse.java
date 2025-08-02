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
    private String period;       // 날짜, 월, 연도
    private Long users;           // 등록된 사용자 수
    private Long problems;        // 등록된 문제 수
    private Long submissions;     // 제출 수
    private Long activeUsers;     // 특정 기간 내 로그인한 사용자 수
}


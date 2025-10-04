package com.ns.solve.domain.dto.contest;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestStatisticsDto {
    private int ongoingContests; // 현재 진행중인 대회 수
    private int monthlyParticipants; // 이번 달 참가자 수
    private int monthlyContests; // 이번 달 대회 수
    private int monthlyWinners; // 이번 달 수상자 수
}
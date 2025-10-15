package com.ns.solve.domain.dto.contest;

import lombok.Getter;

import javax.annotation.Nullable;

@Getter
public class JoinTeamRequest {
    private String teamName;
    private String teamPassword;
    private Long userId;
    @Nullable
    private Long affiliationId;
}
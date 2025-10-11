package com.ns.solve.domain.dto.contest;

import lombok.Getter;

import javax.annotation.Nullable;

@Getter
public class JoinTeamRequest {
    private Long teamId;
    private Long userId;
    @Nullable
    private Long affiliationId;
}
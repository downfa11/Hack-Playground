package com.ns.solve.domain.dto.user;

import lombok.Getter;

@Getter
public class JoinAffiliationRequest {
    private Long userId;
    private Long affiliationId;
}
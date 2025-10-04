package com.ns.solve.domain.dto.contest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinGroupContestRequest {
    private Long userId;
    private Long affiliationId;
}
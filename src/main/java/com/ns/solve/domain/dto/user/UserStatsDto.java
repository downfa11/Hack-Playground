package com.ns.solve.domain.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class UserStatsDto {
    private int rank;
    private int solvedCount;
    private Map<String, Integer> fieldSolvedCounts;
}

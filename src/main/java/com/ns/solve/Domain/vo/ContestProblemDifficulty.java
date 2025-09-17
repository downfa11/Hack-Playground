package com.ns.solve.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContestProblemDifficulty {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard");

    private final String difficulty;
}

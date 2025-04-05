package com.ns.solve.domain.problem;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProblemType {
    WARGAME("wargame"), ALGORITHM("algorithm"), ASSIGNMENT("assignment");


    private final String typeName;
}

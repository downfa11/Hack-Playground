package com.ns.solve.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProblemType {
    WARGAME("wargame"), ALGORITHM("algorithm"), ASSIGNMENT("assignment");


    private final String typeName;
}
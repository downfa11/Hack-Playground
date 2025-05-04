package com.ns.solve.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommentType {
    PROBLEM("problem"), BOARD("board");

    private final String typeName;
}
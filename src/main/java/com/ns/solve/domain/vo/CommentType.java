package com.ns.solve.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommentType {
    PROBLEM("problem"), BOARD("board");

    private final String typeName;
}
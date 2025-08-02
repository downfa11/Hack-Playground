package com.ns.solve.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoardType {
    ANNOUNCE("announce"), FREE("free");

    private final String typeName;
}
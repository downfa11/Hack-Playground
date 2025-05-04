package com.ns.solve.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoardType {
    ANNOUNCE("announce"), FREE("free");

    private final String typeName;
}
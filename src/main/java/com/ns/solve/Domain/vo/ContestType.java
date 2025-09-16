package com.ns.solve.domain.vo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ContestType {
    INDIVIDUAL("individual"),
    TEAM("team"),
    GROUP("group");

    private final String value;

    public String getValue() {
        return value;
    }
}
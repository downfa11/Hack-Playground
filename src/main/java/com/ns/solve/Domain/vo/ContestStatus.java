package com.ns.solve.domain.vo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ContestStatus {
    UPCOMING("upcoming"),
    ONGOING("ongoing"),
    ENDED("ended");

    private final String value;

    public String getValue() {
        return value;
    }
}
package com.ns.solve.domain.vo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AffiliationType {
    UNIVERSITY("학교"),
    COMPANY("기업"),
    ORGANIZATION("단체"),
    CLUB("동아리");

    private final String value;

    public String getValue() {
        return value;
    }
}
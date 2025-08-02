package com.ns.solve.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum Status {

    ACTIVED("활성"), INACTIVED("비활성"), DELETED("삭제");

    private final String typeName;
}
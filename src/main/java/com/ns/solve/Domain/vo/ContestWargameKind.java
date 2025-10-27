package com.ns.solve.domain.vo;

import com.ns.solve.domain.entity.DomainKind;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContestWargameKind implements DomainKind {
    WEBHACKING("웹해킹"), SYSTEM("시스템해킹"), REVERSING("리버싱"), CRYPTO("암호학"), MISC("MISC"), FORENSIC("포렌식"),UNKNOWN("UNKNOWN");

    private final String typeName;
}
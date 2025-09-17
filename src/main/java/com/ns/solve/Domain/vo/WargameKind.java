package com.ns.solve.domain.vo;

import com.ns.solve.domain.entity.problem.DomainKind;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WargameKind  implements DomainKind {
    WEBHACKING("웹해킹"), SYSTEM("시스템해킹"), REVERSING("리버싱"), CRYPTO("암호학"), MISC("MISC"), UNKNOWN("UNKNOWN");

    private final String typeName;
}
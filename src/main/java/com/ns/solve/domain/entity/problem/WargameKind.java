package com.ns.solve.domain.entity.problem;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WargameKind  implements DomainKind {
    WEBHACKING("웹해킹"), SYSTEM("시스템해킹"), REVERSING("리버싱"), CRYPTO("암호학");

    private final String typeName;
}

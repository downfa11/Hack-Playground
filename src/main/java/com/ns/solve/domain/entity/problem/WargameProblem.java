package com.ns.solve.domain.entity.problem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class WargameProblem extends Problem {
    @Enumerated(EnumType.STRING)
    private WargameKind kind;  // 웹해킹, 시스템해킹, 리버싱, 암호학
    private String level;
    private String flag;

    @Column(nullable = true)
    private String dockerfileLink;

    @Column(nullable = true)
    private String problemFile;

    @Column(nullable = true)
    private Long problemFileSize;

    @Override
    public Optional<DomainKind> getDomainKind() {
        return Optional.ofNullable(kind);
    }
}
package com.ns.solve.domain.entity.contest;

import com.ns.solve.domain.entity.problem.Problem;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "contest_problems")
@EqualsAndHashCode(callSuper = true)
public class ContestProblem extends Problem {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    private Contest contest;

    @Column(nullable = false)
    private String flag;

    @Column(nullable = true)
    private String dockerfileLink;

    @Column(nullable = true)
    private String problemFile;

    // ... ContestProblem에만 필요한 필드 추가
}

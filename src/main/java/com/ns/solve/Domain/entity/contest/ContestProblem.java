package com.ns.solve.domain.entity.contest;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.domain.vo.ContestProblemDifficulty;
import com.ns.solve.domain.vo.ContestWargameKind;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "contest_problems")
@EqualsAndHashCode(callSuper = true)
public class ContestProblem extends Problem {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    private Contest contest;

    @Column(nullable = false)
    private String flag;

    @Column(nullable = true)
    private String dockerfileLink;

    @Column(nullable = true)
    private String problemFile;

    @Column(nullable = true)
    private Long problemFileSize;

    private Integer points;

    @Enumerated(EnumType.STRING)
    private ContestProblemDifficulty difficulty;

    @Enumerated(EnumType.STRING)
    private ContestWargameKind kind;
    private boolean isLocked;
}
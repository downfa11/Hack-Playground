package com.ns.solve.domain.entity.contest;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.domain.entity.problem.WargameProblem;
import com.ns.solve.domain.vo.ContestProblemDifficulty;
import com.ns.solve.domain.vo.WargameKind;
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

    private Integer points;

    @Enumerated(EnumType.STRING)
    private ContestProblemDifficulty difficulty;

    @Enumerated(EnumType.STRING)
    private WargameKind kind;
    private boolean isLocked;

    public WargameProblem convertToPlatformProblem(String contestName) {
        WargameProblem platformProblem = new WargameProblem();

        platformProblem.setTitle(this.getTitle());
        platformProblem.setDetail(this.getDetail());
        platformProblem.setType(this.getType()); // ProblemType (Wargame, Assignment, Algorithm)
        platformProblem.setCreator(this.getCreator());
        platformProblem.setEntireCount(this.getEntireCount());
        platformProblem.setCorrectCount(this.getCorrectCount());
        platformProblem.setTags(this.getTags());
        platformProblem.setContainerResourceType(this.getContainerResourceType());
        platformProblem.setPortNumber(this.getPortNumber());
        platformProblem.setResourceLimit(this.getResourceLimit());
        platformProblem.setCreatedAt(this.getCreatedAt());
        platformProblem.setUpdatedAt(this.getUpdatedAt());

        platformProblem.setKind(this.getDomainKind().map(domainKind -> (WargameKind) domainKind).orElse(null));
        platformProblem.setFlag(this.getFlag());
        platformProblem.setDockerfileLink(this.getDockerfileLink());
        platformProblem.setProblemFile(this.getProblemFile());

        platformProblem.setSource(contestName);
        platformProblem.setIsChecked(false);

        return platformProblem;
    }
}
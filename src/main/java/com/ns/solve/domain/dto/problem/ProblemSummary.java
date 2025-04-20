package com.ns.solve.domain.dto.problem;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProblemSummary {
    private Long id;
    private Boolean solved; // 내가 문제를 풀었는지 여부
    private String title;
    private String level;
    private Double correctRate;
    private String creator;
    private String type;
    private LocalDateTime lastModified;



    @QueryProjection
    public ProblemSummary(Long id, String title, String level, Double correctRate, String creator, String type, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.level = level;
        this.correctRate = correctRate;
        this.creator = creator;
        this.type = type;
        this.lastModified = updatedAt;
    }

}

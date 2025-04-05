package com.ns.solve.domain.dto.problem;

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
}

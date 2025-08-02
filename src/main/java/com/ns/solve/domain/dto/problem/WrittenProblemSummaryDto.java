package com.ns.solve.domain.dto.problem;

import com.ns.solve.domain.entity.problem.ProblemType;
import com.ns.solve.domain.entity.problem.WargameKind;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class WrittenProblemSummaryDto {
    private Long id;
    private String title;
    private ProblemType type;
    private WargameKind kind;
    private Integer level;
    private List<String> tags;
    private LocalDateTime createdAt;
    private String reviewStatus; // "APPROVED", "PENDING", "REJECTED"
    private String lastReviewComment;
}
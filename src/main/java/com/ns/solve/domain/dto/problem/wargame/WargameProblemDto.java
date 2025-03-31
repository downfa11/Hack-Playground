package com.ns.solve.domain.dto.problem.wargame;

import java.time.LocalDateTime;
import java.util.List;

import com.ns.solve.domain.problem.WargameProblem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WargameProblemDto {
    private Long id;
    private String title;
    private Boolean isChecked;
    private String creator;
    private Integer attemptCount;
    private Double entireCount;
    private Double correctCount;
    private String source;
    private String reviewer;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String level;
    private String flag;
    private String dockerfileLink;
    private String problemFile;
}

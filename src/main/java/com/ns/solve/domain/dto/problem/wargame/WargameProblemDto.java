package com.ns.solve.domain.dto.problem.wargame;

import java.time.LocalDateTime;
import java.util.List;

import com.ns.solve.domain.Comment;
import com.ns.solve.domain.problem.ProblemType;
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

    private ProblemType type;  // wargame, assignment, algorithm

    private String creator;
    private String detail;

    private Integer attemptCount;
    private Double entireCount;
    private Double correctCount;

    private String source;
    private String reviewer;

    private List<Comment> commentList;
    private List<String> tags;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String kind; // 웹해킹, 시스템해킹, 리버싱, 암호학
    private String level;
    private String flag;

    private String dockerfileLink;
    private String problemFile;
    private Long probelmFileSize;

}

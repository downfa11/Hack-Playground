package com.ns.solve.domain.dto.problem.wargame;

import java.time.LocalDateTime;
import java.util.List;

import com.ns.solve.domain.dto.problem.ProblemDto;
import com.ns.solve.domain.entity.Comment;
import com.ns.solve.domain.vo.BoardType;
import com.ns.solve.domain.vo.WargameKind;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class WargameProblemDto extends ProblemDto {
    private Double entireCount;
    private Double correctCount;

    private String reviewer;
    private List<Comment> commentList;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private WargameKind kind; // 웹해킹, 시스템해킹, 리버싱, 암호학
    private Integer level;

    private String problemFile;
    private Long problemFileSize;
    private boolean hasContainer;
}

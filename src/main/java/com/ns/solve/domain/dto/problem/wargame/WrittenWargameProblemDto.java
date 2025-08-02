package com.ns.solve.domain.dto.problem.wargame;

import com.ns.solve.domain.entity.problem.WargameKind;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WrittenWargameProblemDto {
    private Long id;
    private String title;
    private String detail;
    private WargameKind kind;
    private Integer level;
    private List<String> tags;
    private String problemFile;
    private String dockerfileLink;
    private String flag;
    private String source;
}

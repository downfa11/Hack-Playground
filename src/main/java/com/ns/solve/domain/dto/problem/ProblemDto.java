package com.ns.solve.domain.dto.problem;

import com.ns.solve.domain.vo.BoardType;
import com.ns.solve.domain.vo.ProblemType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor
public class ProblemDto {
    private Long id;
    private String creator;

    private String title;
    private ProblemType type;
    private String detail;
    private String source;
    private List<String> tags;
}

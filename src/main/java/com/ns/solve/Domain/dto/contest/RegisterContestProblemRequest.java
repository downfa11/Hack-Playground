package com.ns.solve.domain.dto.contest;

import com.ns.solve.domain.vo.ProblemType;
import com.ns.solve.domain.vo.WargameKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RegisterContestProblemRequest {

    @NotBlank(message = "문제 제목은 필수입니다.")
    private String title;

    @NotNull(message = "문제 타입은 필수입니다.")
    private ProblemType type;

    @NotBlank(message = "문제 내용은 필수입니다.")
    private String detail;

    private List<String> tags;

    @NotNull(message = "점수는 필수입니다.")
    private Integer score;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    private WargameKind kind;
    private String flag;
    private String dockerfileLink;
    private String problemFile;
}

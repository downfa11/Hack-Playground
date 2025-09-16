package com.ns.solve.domain.dto.contest;

import com.ns.solve.domain.vo.WargameKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class ModifyContestProblemRequest {

    @NotBlank(message = "문제 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "문제 내용은 필수입니다.")
    private String detail;

    @NotNull(message = "점수는 필수입니다.")
    private Integer score;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    private List<String> tags;

    private WargameKind kind;
    private String flag;
    private String dockerfileLink;
    private String problemFile;
}
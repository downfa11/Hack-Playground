package com.ns.solve.domain.dto.contest;

import com.ns.solve.domain.vo.ContestProblemDifficulty;
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
    private Integer points;

    private List<String> tags;

    @NotBlank(message = "문제 유형은 필수입니다.")
    private WargameKind kind;

    @NotBlank(message = "플래그(Flag)는 필수입니다.")
    private String flag;

    private int portNumber;
    private String dockerfileLink;
    private ContestProblemDifficulty difficulty;
}
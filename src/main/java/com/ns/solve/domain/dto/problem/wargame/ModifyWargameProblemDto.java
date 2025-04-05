package com.ns.solve.domain.dto.problem.wargame;

import com.ns.solve.domain.dto.problem.ModifyProblemDto;
import com.ns.solve.domain.dto.problem.RegisterProblemDto;
import lombok.Getter;

@Getter
public class ModifyWargameProblemDto extends ModifyProblemDto {
    private Long probelmId;
    private String kind;  // 웹해킹, 시스템해킹, 리버싱, 암호학
    private String level;
    private String flag;

    private String dockerfileLink;

    ModifyWargameProblemDto() {
        super();
    }
}

package com.ns.solve.domain.dto.problem.wargame;

import com.ns.solve.domain.dto.problem.RegisterProblemDto;
import com.ns.solve.domain.entity.problem.WargameKind;
import lombok.Getter;

@Getter
public class RegisterWargameProblemDto extends RegisterProblemDto {

    private WargameKind kind;  // 웹해킹, 시스템해킹, 리버싱, 암호학
    private String level;
    private String flag;

    private String dockerfileLink;

    RegisterWargameProblemDto() {
        super();
    }
}

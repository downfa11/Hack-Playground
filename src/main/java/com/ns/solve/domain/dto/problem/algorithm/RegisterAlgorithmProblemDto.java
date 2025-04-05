package com.ns.solve.domain.dto.problem.algorithm;

import com.ns.solve.domain.dto.problem.RegisterProblemDto;


public class RegisterAlgorithmProblemDto extends RegisterProblemDto {
    private String difficulty;
    private String hint;

    RegisterAlgorithmProblemDto() {
        super();
    }
}

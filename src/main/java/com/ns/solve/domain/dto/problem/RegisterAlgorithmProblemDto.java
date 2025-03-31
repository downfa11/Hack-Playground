package com.ns.solve.domain.dto.problem;

import com.ns.solve.domain.problem.ProblemType;
import jakarta.validation.constraints.NotNull;

import java.util.List;


public class RegisterAlgorithmProblemDto extends RegisterProblemDto {
    private String difficulty;
    private String hint;

    RegisterAlgorithmProblemDto() {
        super();
    }
}

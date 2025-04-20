package com.ns.solve.domain.dto.problem;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ns.solve.domain.dto.problem.algorithm.RegisterAlgorithmProblemDto;
import com.ns.solve.domain.dto.problem.wargame.RegisterWargameProblemDto;
import com.ns.solve.domain.entity.problem.ProblemType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RegisterWargameProblemDto.class, name = "WARGAME"),
        @JsonSubTypes.Type(value = RegisterAlgorithmProblemDto.class, name = "ALGORITHM")
})
public class RegisterProblemDto {

    @NotNull
    private String title;
    @NotNull
    private ProblemType type;
    @NotNull
    private String detail;

    private String source;
    private List<String> tags;
}
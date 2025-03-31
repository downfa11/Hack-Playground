package com.ns.solve.domain.dto.problem;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ns.solve.domain.dto.problem.wargame.RegisterWargameProblemDto;
import com.ns.solve.domain.problem.ProblemType;
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
    private ProblemType type;

    private String title;
    private String creator;

    private String detail;
    private String source;
    private String reviewer;

    private List<String> tags;
}
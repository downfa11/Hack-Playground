package com.ns.solve.domain.dto.problem;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ns.solve.domain.dto.problem.wargame.ModifyWargameProblemDto;
import com.ns.solve.domain.dto.problem.wargame.RegisterWargameProblemDto;
import com.ns.solve.domain.problem.ProblemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ModifyWargameProblemDto.class, name = "WARGAME"),
})
public class ModifyProblemDto {
    private Long problemId;
    private String title;
    private ProblemType type;
    private String creator;
    private String solution;
    private String detail;
    private List<String> tags;
}
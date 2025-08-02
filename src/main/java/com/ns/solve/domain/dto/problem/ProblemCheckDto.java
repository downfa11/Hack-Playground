package com.ns.solve.domain.dto.problem;

import com.ns.solve.domain.entity.problem.ContainerResourceType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProblemCheckDto {
    private ContainerResourceType containerResourceType; // dedicated, shared
    private Integer portNumber;
    private Integer cpuLimit;
    private Integer memoryLimit;

    private String reviewComment;
    private Boolean approved;
}

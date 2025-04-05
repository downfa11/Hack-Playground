package com.ns.solve.domain.dto.problem.assignment;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CaseDto {
    private Long problemId;
    private List<String> inputList;
    private List<String> outputList;
    private Long bias;
}

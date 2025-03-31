package com.ns.solve.utils;

import com.ns.solve.domain.dto.problem.ProblemSummary;
import com.ns.solve.domain.dto.problem.wargame.WargameProblemDto;
import com.ns.solve.domain.problem.Problem;
import com.ns.solve.domain.problem.WargameProblem;

import java.util.List;
import java.util.stream.Collectors;

public class ProblemMapper {

    public static ProblemSummary toProblemSummary(Problem problem) {
        if (problem instanceof WargameProblem) {
            return toWargameProblemSummary((WargameProblem) problem);
        } else {
            return ProblemSummary.builder()
                    .id(problem.getId())
                    .title(problem.getTitle())
                    .creator(problem.getCreator())
                    .type(problem.getType().getTypeName())
                    .correctRate(problem.getCorrectCount() / problem.getEntireCount())
                    .build();
        }
    }

    public static ProblemSummary toWargameProblemSummary(WargameProblem problem) {
        return ProblemSummary.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .creator(problem.getCreator())
                .type(problem.getType().getTypeName())
                .correctRate(problem.getCorrectCount() / problem.getEntireCount())
                .level(problem.getLevel())
                .build();
    }

    public static List<ProblemSummary> toProblemSummaryList(List<Problem> problems) {
        return problems.stream()
                .map(ProblemMapper::toProblemSummary)
                .collect(Collectors.toList());
    }

    public static WargameProblemDto toWargameProblemDto(WargameProblem problem) {
        return new WargameProblemDto(
                problem.getId(),
                problem.getTitle(),
                problem.getIsChecked(),
                problem.getCreator(),
                problem.getAttemptCount(),
                problem.getEntireCount(),
                problem.getCorrectCount(),
                problem.getSource(),
                problem.getReviewer(),
                problem.getTags(),
                problem.getCreatedAt(),
                problem.getUpdatedAt(),
                problem.getLevel(),
                problem.getFlag(),
                problem.getDockerfileLink(),
                problem.getProblemFile()
        );
    }
}

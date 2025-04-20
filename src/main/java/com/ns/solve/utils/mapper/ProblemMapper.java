package com.ns.solve.utils.mapper;

import com.ns.solve.domain.dto.problem.ProblemDto;
import com.ns.solve.domain.dto.problem.ProblemSummary;
import com.ns.solve.domain.dto.problem.wargame.WargameProblemDto;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.domain.entity.problem.WargameProblem;

import java.util.List;
import java.util.stream.Collectors;

public class ProblemMapper {

    public static ProblemSummary mapperToProblemSummary(Problem problem) {
        if (problem instanceof WargameProblem) {
            return mapperToWargameProblemSummary((WargameProblem) problem);
        } else {
            return ProblemSummary.builder()
                    .id(problem.getId())
                    .title(problem.getTitle())
                    .creator(problem.getCreator().getNickname())
                    .type(problem.getType().getTypeName())
                    .correctRate(problem.getCorrectCount() / problem.getEntireCount())
                    .build();
        }
    }

    public static ProblemSummary mapperToWargameProblemSummary(WargameProblem problem) {
        return ProblemSummary.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .creator(problem.getCreator().getNickname())
                .type(problem.getType().getTypeName())
                .correctRate(problem.getCorrectCount() / problem.getEntireCount())
                .level(problem.getLevel())
                .build();
    }

    public static List<ProblemSummary> mapperToProblemSummaryList(List<Problem> problems) {
        return problems.stream()
                .map(ProblemMapper::mapperToProblemSummary)
                .collect(Collectors.toList());
    }

    public static WargameProblemDto mapperToWargameProblemDto(WargameProblem problem) {
        return WargameProblemDto.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .type(problem.getType())
                .creator(problem.getCreator().getNickname())
                .detail(problem.getDetail())
                .attemptCount(problem.getAttemptCount())
                .entireCount(problem.getEntireCount())
                .correctCount(problem.getCorrectCount())
                .source(problem.getSource())
                .reviewer(problem.getReviewer())
                .commentList(problem.getCommentList())
                .tags(problem.getTags())
                .createdAt(problem.getCreatedAt())
                .updatedAt(problem.getUpdatedAt())
                .kind(problem.getKind())
                .level(problem.getLevel())
                .flag(problem.getFlag())
                .dockerfileLink(problem.getDockerfileLink())
                .problemFile(problem.getProblemFile())
                .probelmFileSize(problem.getProbelmFileSize())
                .build();
    }


    public static ProblemDto mapperToProblemDto(Problem problem) {
        return ProblemDto.builder()
                .title(problem.getTitle())
                .type(problem.getType())
                .detail(problem.getDetail())
                .source(problem.getSource())
                .tags(problem.getTags())
                .build();
    }

}

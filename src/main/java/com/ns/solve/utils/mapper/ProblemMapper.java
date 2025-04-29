package com.ns.solve.utils.mapper;

import com.ns.solve.domain.dto.problem.ProblemDto;
import com.ns.solve.domain.dto.problem.ProblemSummary;
import com.ns.solve.domain.dto.problem.wargame.WargameProblemDto;
import com.ns.solve.domain.entity.User;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.domain.entity.problem.WargameProblem;

import java.util.List;
import java.util.Optional;
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
        String reviewerNickname = Optional.ofNullable(problem.getReviewer())
                .map(User::getNickname)
                .orElse("None");


        return WargameProblemDto.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .type(problem.getType())
                .creator(problem.getCreator().getNickname())
                .detail(problem.getDetail())
                .entireCount(problem.getEntireCount())
                .correctCount(problem.getCorrectCount())
                .source(problem.getSource())
                .reviewer(reviewerNickname)
                .commentList(problem.getCommentList())
                .tags(problem.getTags())
                .createdAt(problem.getCreatedAt())
                .updatedAt(problem.getUpdatedAt())
                .kind(problem.getKind())
                .level(problem.getLevel())
                .flag(problem.getFlag())
                .dockerfileLink(problem.getDockerfileLink())
                .problemFile(problem.getProblemFile())
                .probelmFileSize(problem.getProblemFileSize())
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

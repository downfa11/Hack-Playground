package com.ns.solve.utils.mapper;

import com.ns.solve.domain.dto.problem.ProblemDto;
import com.ns.solve.domain.dto.problem.ProblemSummary;
import com.ns.solve.domain.dto.problem.WrittenProblemSummaryDto;
import com.ns.solve.domain.dto.problem.wargame.WargameProblemDto;
import com.ns.solve.domain.dto.problem.wargame.WrittenWargameProblemDto;
import com.ns.solve.domain.entity.admin.ProblemReview;
import com.ns.solve.domain.entity.user.User;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.domain.entity.problem.WargameProblem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Comparator;
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
                    .kind("not have kind in this type") // Todo. 확장한다면 다른 타입별로 다 kind를 부여해야함
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
                .kind(problem.getKind().getTypeName())
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

        Boolean hasContainer = problem.getDockerfileLink() != null && !problem.getDockerfileLink().isBlank();

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
                .problemFile(problem.getProblemFile())
                .problemFileSize(problem.getProblemFileSize())
                .hasContainer(hasContainer)
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

    public static WrittenWargameProblemDto mapperToWrittenWargameProblemDto(WargameProblem wargameProblem) {
        return WrittenWargameProblemDto.builder()
                .id(wargameProblem.getId())
                .title(wargameProblem.getTitle())
                .detail(wargameProblem.getDetail())
                .kind(wargameProblem.getKind())
                .level(wargameProblem.getLevel())
                .flag(wargameProblem.getFlag())
                .dockerfileLink(wargameProblem.getDockerfileLink())
                .problemFile(wargameProblem.getProblemFile())
                .source(wargameProblem.getSource())
                .tags(wargameProblem.getTags())
                .build();
    }

    public static WrittenProblemSummaryDto mapperToWrittenProblemSummaryDto(Problem problem, List<ProblemReview> problemReviews) { // problemReviews 파라미터 추가
        String reviewStatus = "PENDING";
        String lastReviewComment = null;

        if (problem.getIsChecked()) {
            reviewStatus = "APPROVED";
        } else {

            Optional<ProblemReview> lastReview = problemReviews.stream()
                    .sorted(Comparator.comparing(ProblemReview::getCreatedAt).reversed())
                    .findFirst();

            if (lastReview.isPresent() && !lastReview.get().getIsApproved()) {
                reviewStatus = "REJECTED";
                lastReviewComment = lastReview.get().getComment();
            }
        }

        return WrittenProblemSummaryDto.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .type(problem.getType())
                .kind(problem instanceof WargameProblem ? ((WargameProblem) problem).getKind() : null)
                .level(problem instanceof WargameProblem ? ((WargameProblem) problem).getLevel() : null)
                .tags(problem.getTags())
                .createdAt(problem.getCreatedAt())
                .reviewStatus(reviewStatus)
                .lastReviewComment(lastReviewComment)
                .build();
    }
}

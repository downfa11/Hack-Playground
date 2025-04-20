package com.ns.solve.domain.dto.problem;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.ns.solve.domain.dto.problem.QProblemSummary is a Querydsl Projection type for ProblemSummary
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QProblemSummary extends ConstructorExpression<ProblemSummary> {

    private static final long serialVersionUID = -1567991516L;

    public QProblemSummary(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> level, com.querydsl.core.types.Expression<Double> correctRate, com.querydsl.core.types.Expression<String> creator, com.querydsl.core.types.Expression<String> type, com.querydsl.core.types.Expression<java.time.LocalDateTime> updatedAt) {
        super(ProblemSummary.class, new Class<?>[]{long.class, String.class, String.class, double.class, String.class, String.class, java.time.LocalDateTime.class}, id, title, level, correctRate, creator, type, updatedAt);
    }

}


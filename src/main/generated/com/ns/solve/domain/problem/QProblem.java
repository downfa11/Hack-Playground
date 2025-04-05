package com.ns.solve.domain.problem;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProblem is a Querydsl query type for Problem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProblem extends EntityPathBase<Problem> {

    private static final long serialVersionUID = 2047721137L;

    public static final QProblem problem = new QProblem("problem");

    public final NumberPath<Integer> attemptCount = createNumber("attemptCount", Integer.class);

    public final ListPath<com.ns.solve.domain.Comment, com.ns.solve.domain.QComment> commentList = this.<com.ns.solve.domain.Comment, com.ns.solve.domain.QComment>createList("commentList", com.ns.solve.domain.Comment.class, com.ns.solve.domain.QComment.class, PathInits.DIRECT2);

    public final NumberPath<Double> correctCount = createNumber("correctCount", Double.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath creator = createString("creator");

    public final StringPath detail = createString("detail");

    public final NumberPath<Double> entireCount = createNumber("entireCount", Double.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isChecked = createBoolean("isChecked");

    public final StringPath reviewer = createString("reviewer");

    public final StringPath source = createString("source");

    public final ListPath<String, StringPath> tags = this.<String, StringPath>createList("tags", String.class, StringPath.class, PathInits.DIRECT2);

    public final StringPath title = createString("title");

    public final EnumPath<ProblemType> type = createEnum("type", ProblemType.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QProblem(String variable) {
        super(Problem.class, forVariable(variable));
    }

    public QProblem(Path<? extends Problem> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProblem(PathMetadata metadata) {
        super(Problem.class, metadata);
    }

}


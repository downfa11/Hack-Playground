package com.ns.solve.domain.entity.problem;

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

    private static final long serialVersionUID = 433174148L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProblem problem = new QProblem("problem");

    public final NumberPath<Integer> attemptCount = createNumber("attemptCount", Integer.class);

    public final ListPath<com.ns.solve.domain.entity.Comment, com.ns.solve.domain.entity.QComment> commentList = this.<com.ns.solve.domain.entity.Comment, com.ns.solve.domain.entity.QComment>createList("commentList", com.ns.solve.domain.entity.Comment.class, com.ns.solve.domain.entity.QComment.class, PathInits.DIRECT2);

    public final NumberPath<Double> correctCount = createNumber("correctCount", Double.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final com.ns.solve.domain.entity.QUser creator;

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
        this(Problem.class, forVariable(variable), INITS);
    }

    public QProblem(Path<? extends Problem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProblem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProblem(PathMetadata metadata, PathInits inits) {
        this(Problem.class, metadata, inits);
    }

    public QProblem(Class<? extends Problem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.creator = inits.isInitialized("creator") ? new com.ns.solve.domain.entity.QUser(forProperty("creator")) : null;
    }

}


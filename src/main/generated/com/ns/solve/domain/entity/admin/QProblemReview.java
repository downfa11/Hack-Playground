package com.ns.solve.domain.entity.admin;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProblemReview is a Querydsl query type for ProblemReview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProblemReview extends EntityPathBase<ProblemReview> {

    private static final long serialVersionUID = 395851948L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProblemReview problemReview = new QProblemReview("problemReview");

    public final StringPath comment = createString("comment");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isApproved = createBoolean("isApproved");

    public final com.ns.solve.domain.entity.problem.QProblem problem;

    public final com.ns.solve.domain.entity.user.QUser reviewer;

    public QProblemReview(String variable) {
        this(ProblemReview.class, forVariable(variable), INITS);
    }

    public QProblemReview(Path<? extends ProblemReview> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProblemReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProblemReview(PathMetadata metadata, PathInits inits) {
        this(ProblemReview.class, metadata, inits);
    }

    public QProblemReview(Class<? extends ProblemReview> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.problem = inits.isInitialized("problem") ? new com.ns.solve.domain.entity.problem.QProblem(forProperty("problem"), inits.get("problem")) : null;
        this.reviewer = inits.isInitialized("reviewer") ? new com.ns.solve.domain.entity.user.QUser(forProperty("reviewer")) : null;
    }

}


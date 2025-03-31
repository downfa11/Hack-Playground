package com.ns.solve.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSolved is a Querydsl query type for Solved
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSolved extends EntityPathBase<Solved> {

    private static final long serialVersionUID = 1719199364L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSolved solved = new QSolved("solved");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath solve = createBoolean("solve");

    public final com.ns.solve.domain.problem.QProblem solvedProblem;

    public final DateTimePath<java.time.LocalDateTime> solvedTime = createDateTime("solvedTime", java.time.LocalDateTime.class);

    public final QUser solvedUser;

    public QSolved(String variable) {
        this(Solved.class, forVariable(variable), INITS);
    }

    public QSolved(Path<? extends Solved> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSolved(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSolved(PathMetadata metadata, PathInits inits) {
        this(Solved.class, metadata, inits);
    }

    public QSolved(Class<? extends Solved> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.solvedProblem = inits.isInitialized("solvedProblem") ? new com.ns.solve.domain.problem.QProblem(forProperty("solvedProblem")) : null;
        this.solvedUser = inits.isInitialized("solvedUser") ? new QUser(forProperty("solvedUser")) : null;
    }

}


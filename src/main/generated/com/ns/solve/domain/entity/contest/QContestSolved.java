package com.ns.solve.domain.entity.contest;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QContestSolved is a Querydsl query type for ContestSolved
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QContestSolved extends EntityPathBase<ContestSolved> {

    private static final long serialVersionUID = -111044589L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QContestSolved contestSolved = new QContestSolved("contestSolved");

    public final QContest contest;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QContestProblem solvedProblem;

    public final DateTimePath<java.time.LocalDateTime> solvedTime = createDateTime("solvedTime", java.time.LocalDateTime.class);

    public final com.ns.solve.domain.entity.user.QUser solvedUser;

    public final QTeam team;

    public QContestSolved(String variable) {
        this(ContestSolved.class, forVariable(variable), INITS);
    }

    public QContestSolved(Path<? extends ContestSolved> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QContestSolved(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QContestSolved(PathMetadata metadata, PathInits inits) {
        this(ContestSolved.class, metadata, inits);
    }

    public QContestSolved(Class<? extends ContestSolved> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.contest = inits.isInitialized("contest") ? new QContest(forProperty("contest")) : null;
        this.solvedProblem = inits.isInitialized("solvedProblem") ? new QContestProblem(forProperty("solvedProblem"), inits.get("solvedProblem")) : null;
        this.solvedUser = inits.isInitialized("solvedUser") ? new com.ns.solve.domain.entity.user.QUser(forProperty("solvedUser")) : null;
        this.team = inits.isInitialized("team") ? new QTeam(forProperty("team"), inits.get("team")) : null;
    }

}


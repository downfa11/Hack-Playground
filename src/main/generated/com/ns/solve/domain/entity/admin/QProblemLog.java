package com.ns.solve.domain.entity.admin;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QProblemLog is a Querydsl query type for ProblemLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProblemLog extends EntityPathBase<ProblemLog> {

    private static final long serialVersionUID = -910425488L;

    public static final QProblemLog problemLog = new QProblemLog("problemLog");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath creatorUsername = createString("creatorUsername");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.ns.solve.domain.vo.OperationType> operationType = createEnum("operationType", com.ns.solve.domain.vo.OperationType.class);

    public final NumberPath<Long> problemId = createNumber("problemId", Long.class);

    public final StringPath problemTitle = createString("problemTitle");

    public final BooleanPath reported = createBoolean("reported");

    public QProblemLog(String variable) {
        super(ProblemLog.class, forVariable(variable));
    }

    public QProblemLog(Path<? extends ProblemLog> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProblemLog(PathMetadata metadata) {
        super(ProblemLog.class, metadata);
    }

}


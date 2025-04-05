package com.ns.solve.domain.problem;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCase is a Querydsl query type for Case
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCase extends EntityPathBase<Case> {

    private static final long serialVersionUID = 2054375518L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCase case$ = new QCase("case$");

    public final NumberPath<Long> bias = createNumber("bias", Long.class);

    public final StringPath caseDetail = createString("caseDetail");

    public final NumberPath<Long> caseId = createNumber("caseId", Long.class);

    public final EnumPath<Case.CaseKind> caseKind = createEnum("caseKind", Case.CaseKind.class);

    public final StringPath expectedOutput = createString("expectedOutput");

    public final StringPath input = createString("input");

    public final QProblem problem;

    public QCase(String variable) {
        this(Case.class, forVariable(variable), INITS);
    }

    public QCase(Path<? extends Case> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCase(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCase(PathMetadata metadata, PathInits inits) {
        this(Case.class, metadata, inits);
    }

    public QCase(Class<? extends Case> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.problem = inits.isInitialized("problem") ? new QProblem(forProperty("problem")) : null;
    }

}


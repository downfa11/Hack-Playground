package com.ns.solve.domain.entity.problem;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAssignmentProblem is a Querydsl query type for AssignmentProblem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAssignmentProblem extends EntityPathBase<AssignmentProblem> {

    private static final long serialVersionUID = -141747017L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAssignmentProblem assignmentProblem = new QAssignmentProblem("assignmentProblem");

    public final QProblem _super;

    public final ListPath<Case, QCase> caseList = this.<Case, QCase>createList("caseList", Case.class, QCase.class, PathInits.DIRECT2);

    //inherited
    public final ListPath<com.ns.solve.domain.entity.Comment, com.ns.solve.domain.entity.QComment> commentList;

    //inherited
    public final EnumPath<ContainerResourceType> containerResourceType;

    //inherited
    public final NumberPath<Double> correctCount;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt;

    // inherited
    public final com.ns.solve.domain.entity.QUser creator;

    public final DateTimePath<java.sql.Timestamp> deadline = createDateTime("deadline", java.sql.Timestamp.class);

    //inherited
    public final StringPath detail;

    //inherited
    public final NumberPath<Double> entireCount;

    //inherited
    public final NumberPath<Long> id;

    public final StringPath instruction = createString("instruction");

    //inherited
    public final BooleanPath isChecked;

    public final BooleanPath isPublic = createBoolean("isPublic");

    //inherited
    public final NumberPath<Integer> portNumber;

    //inherited
    public final MapPath<String, Integer, NumberPath<Integer>> resourceLimit;

    // inherited
    public final com.ns.solve.domain.entity.QUser reviewer;

    //inherited
    public final StringPath source;

    public final StringPath submissionLink = createString("submissionLink");

    //inherited
    public final ListPath<String, StringPath> tags;

    //inherited
    public final StringPath title;

    //inherited
    public final EnumPath<ProblemType> type;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt;

    public QAssignmentProblem(String variable) {
        this(AssignmentProblem.class, forVariable(variable), INITS);
    }

    public QAssignmentProblem(Path<? extends AssignmentProblem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAssignmentProblem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAssignmentProblem(PathMetadata metadata, PathInits inits) {
        this(AssignmentProblem.class, metadata, inits);
    }

    public QAssignmentProblem(Class<? extends AssignmentProblem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QProblem(type, metadata, inits);
        this.commentList = _super.commentList;
        this.containerResourceType = _super.containerResourceType;
        this.correctCount = _super.correctCount;
        this.createdAt = _super.createdAt;
        this.creator = _super.creator;
        this.detail = _super.detail;
        this.entireCount = _super.entireCount;
        this.id = _super.id;
        this.isChecked = _super.isChecked;
        this.portNumber = _super.portNumber;
        this.resourceLimit = _super.resourceLimit;
        this.reviewer = _super.reviewer;
        this.source = _super.source;
        this.tags = _super.tags;
        this.title = _super.title;
        this.type = _super.type;
        this.updatedAt = _super.updatedAt;
    }

}


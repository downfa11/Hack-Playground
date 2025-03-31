package com.ns.solve.domain.problem;

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

    private static final long serialVersionUID = -1902997340L;

    public static final QAssignmentProblem assignmentProblem = new QAssignmentProblem("assignmentProblem");

    public final QProblem _super = new QProblem(this);

    //inherited
    public final NumberPath<Integer> attemptCount = _super.attemptCount;

    public final ListPath<Case, QCase> caseList = this.<Case, QCase>createList("caseList", Case.class, QCase.class, PathInits.DIRECT2);

    //inherited
    public final ListPath<com.ns.solve.domain.Comment, com.ns.solve.domain.QComment> commentList = _super.commentList;

    //inherited
    public final NumberPath<Double> correctCount = _super.correctCount;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath creator = _super.creator;

    public final DateTimePath<java.sql.Timestamp> deadline = createDateTime("deadline", java.sql.Timestamp.class);

    //inherited
    public final StringPath detail = _super.detail;

    //inherited
    public final NumberPath<Double> entireCount = _super.entireCount;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath instruction = createString("instruction");

    //inherited
    public final BooleanPath isChecked = _super.isChecked;

    public final BooleanPath isPublic = createBoolean("isPublic");

    //inherited
    public final StringPath reviewer = _super.reviewer;

    //inherited
    public final StringPath source = _super.source;

    public final StringPath submissionLink = createString("submissionLink");

    //inherited
    public final ListPath<String, StringPath> tags = _super.tags;

    //inherited
    public final StringPath title = _super.title;

    //inherited
    public final EnumPath<ProblemType> type = _super.type;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QAssignmentProblem(String variable) {
        super(AssignmentProblem.class, forVariable(variable));
    }

    public QAssignmentProblem(Path<? extends AssignmentProblem> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAssignmentProblem(PathMetadata metadata) {
        super(AssignmentProblem.class, metadata);
    }

}


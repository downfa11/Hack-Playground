package com.ns.solve.domain.entity.problem;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAlgorithmProblem is a Querydsl query type for AlgorithmProblem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAlgorithmProblem extends EntityPathBase<AlgorithmProblem> {

    private static final long serialVersionUID = 1070807275L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAlgorithmProblem algorithmProblem = new QAlgorithmProblem("algorithmProblem");

    public final QProblem _super;

    //inherited
    public final NumberPath<Integer> attemptCount;

    public final ListPath<Case, QCase> caseList = this.<Case, QCase>createList("caseList", Case.class, QCase.class, PathInits.DIRECT2);

    //inherited
    public final ListPath<com.ns.solve.domain.entity.Comment, com.ns.solve.domain.entity.QComment> commentList;

    //inherited
    public final NumberPath<Double> correctCount;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt;

    // inherited
    public final com.ns.solve.domain.entity.QUser creator;

    //inherited
    public final StringPath detail;

    //inherited
    public final NumberPath<Double> entireCount;

    //inherited
    public final NumberPath<Long> id;

    public final StringPath inputOutputSpecification = createString("inputOutputSpecification");

    //inherited
    public final BooleanPath isChecked;

    public final NumberPath<Long> level = createNumber("level", Long.class);

    //inherited
    public final StringPath reviewer;

    //inherited
    public final StringPath source;

    //inherited
    public final ListPath<String, StringPath> tags;

    //inherited
    public final StringPath title;

    //inherited
    public final EnumPath<ProblemType> type;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt;

    public QAlgorithmProblem(String variable) {
        this(AlgorithmProblem.class, forVariable(variable), INITS);
    }

    public QAlgorithmProblem(Path<? extends AlgorithmProblem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAlgorithmProblem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAlgorithmProblem(PathMetadata metadata, PathInits inits) {
        this(AlgorithmProblem.class, metadata, inits);
    }

    public QAlgorithmProblem(Class<? extends AlgorithmProblem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QProblem(type, metadata, inits);
        this.attemptCount = _super.attemptCount;
        this.commentList = _super.commentList;
        this.correctCount = _super.correctCount;
        this.createdAt = _super.createdAt;
        this.creator = _super.creator;
        this.detail = _super.detail;
        this.entireCount = _super.entireCount;
        this.id = _super.id;
        this.isChecked = _super.isChecked;
        this.reviewer = _super.reviewer;
        this.source = _super.source;
        this.tags = _super.tags;
        this.title = _super.title;
        this.type = _super.type;
        this.updatedAt = _super.updatedAt;
    }

}


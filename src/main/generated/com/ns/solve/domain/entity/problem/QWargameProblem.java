package com.ns.solve.domain.entity.problem;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWargameProblem is a Querydsl query type for WargameProblem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWargameProblem extends EntityPathBase<WargameProblem> {

    private static final long serialVersionUID = -1716404512L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWargameProblem wargameProblem = new QWargameProblem("wargameProblem");

    public final QProblem _super;

    //inherited
    public final NumberPath<Integer> attemptCount;

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

    public final StringPath dockerfileLink = createString("dockerfileLink");

    //inherited
    public final NumberPath<Double> entireCount;

    public final StringPath flag = createString("flag");

    //inherited
    public final NumberPath<Long> id;

    //inherited
    public final BooleanPath isChecked;

    public final StringPath kind = createString("kind");

    public final StringPath level = createString("level");

    public final NumberPath<Long> probelmFileSize = createNumber("probelmFileSize", Long.class);

    public final StringPath problemFile = createString("problemFile");

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

    public QWargameProblem(String variable) {
        this(WargameProblem.class, forVariable(variable), INITS);
    }

    public QWargameProblem(Path<? extends WargameProblem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWargameProblem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWargameProblem(PathMetadata metadata, PathInits inits) {
        this(WargameProblem.class, metadata, inits);
    }

    public QWargameProblem(Class<? extends WargameProblem> type, PathMetadata metadata, PathInits inits) {
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


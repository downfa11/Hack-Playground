package com.ns.solve.domain.problem;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QWargameProblem is a Querydsl query type for WargameProblem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWargameProblem extends EntityPathBase<WargameProblem> {

    private static final long serialVersionUID = 166828563L;

    public static final QWargameProblem wargameProblem = new QWargameProblem("wargameProblem");

    public final QProblem _super = new QProblem(this);

    //inherited
    public final NumberPath<Integer> attemptCount = _super.attemptCount;

    //inherited
    public final ListPath<com.ns.solve.domain.Comment, com.ns.solve.domain.QComment> commentList = _super.commentList;

    //inherited
    public final NumberPath<Double> correctCount = _super.correctCount;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath creator = _super.creator;

    //inherited
    public final StringPath detail = _super.detail;

    public final StringPath dockerfileLink = createString("dockerfileLink");

    //inherited
    public final NumberPath<Double> entireCount = _super.entireCount;

    public final StringPath flag = createString("flag");

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final BooleanPath isChecked = _super.isChecked;

    public final StringPath kind = createString("kind");

    public final StringPath level = createString("level");

    public final NumberPath<Long> probelmFileSize = createNumber("probelmFileSize", Long.class);

    public final StringPath problemFile = createString("problemFile");

    //inherited
    public final StringPath reviewer = _super.reviewer;

    //inherited
    public final StringPath source = _super.source;

    //inherited
    public final ListPath<String, StringPath> tags = _super.tags;

    //inherited
    public final StringPath title = _super.title;

    //inherited
    public final EnumPath<ProblemType> type = _super.type;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QWargameProblem(String variable) {
        super(WargameProblem.class, forVariable(variable));
    }

    public QWargameProblem(Path<? extends WargameProblem> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWargameProblem(PathMetadata metadata) {
        super(WargameProblem.class, metadata);
    }

}


package com.ns.solve.domain.entity.user;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAffiliation is a Querydsl query type for Affiliation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAffiliation extends EntityPathBase<Affiliation> {

    private static final long serialVersionUID = -927471783L;

    public static final QAffiliation affiliation = new QAffiliation("affiliation");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final EnumPath<com.ns.solve.domain.vo.AffiliationType> type = createEnum("type", com.ns.solve.domain.vo.AffiliationType.class);

    public final SetPath<User, QUser> users = this.<User, QUser>createSet("users", User.class, QUser.class, PathInits.DIRECT2);

    public QAffiliation(String variable) {
        super(Affiliation.class, forVariable(variable));
    }

    public QAffiliation(Path<? extends Affiliation> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAffiliation(PathMetadata metadata) {
        super(Affiliation.class, metadata);
    }

}


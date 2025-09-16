package com.ns.solve.domain.entity.contest;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPrize is a Querydsl query type for Prize
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPrize extends EntityPathBase<Prize> {

    private static final long serialVersionUID = -1846516756L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPrize prize = new QPrize("prize");

    public final QContest contest;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> numberOfWinners = createNumber("numberOfWinners", Integer.class);

    public final SetPath<com.ns.solve.domain.entity.user.User, com.ns.solve.domain.entity.user.QUser> winners = this.<com.ns.solve.domain.entity.user.User, com.ns.solve.domain.entity.user.QUser>createSet("winners", com.ns.solve.domain.entity.user.User.class, com.ns.solve.domain.entity.user.QUser.class, PathInits.DIRECT2);

    public QPrize(String variable) {
        this(Prize.class, forVariable(variable), INITS);
    }

    public QPrize(Path<? extends Prize> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPrize(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPrize(PathMetadata metadata, PathInits inits) {
        this(Prize.class, metadata, inits);
    }

    public QPrize(Class<? extends Prize> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.contest = inits.isInitialized("contest") ? new QContest(forProperty("contest")) : null;
    }

}


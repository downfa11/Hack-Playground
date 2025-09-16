package com.ns.solve.domain.entity.contest;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QContest is a Querydsl query type for Contest
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QContest extends EntityPathBase<Contest> {

    private static final long serialVersionUID = 584799854L;

    public static final QContest contest = new QContest("contest");

    public final SetPath<com.ns.solve.domain.entity.user.Affiliation, com.ns.solve.domain.entity.user.QAffiliation> affiliations = this.<com.ns.solve.domain.entity.user.Affiliation, com.ns.solve.domain.entity.user.QAffiliation>createSet("affiliations", com.ns.solve.domain.entity.user.Affiliation.class, com.ns.solve.domain.entity.user.QAffiliation.class, PathInits.DIRECT2);

    public final SetPath<com.ns.solve.domain.vo.AffiliationType, EnumPath<com.ns.solve.domain.vo.AffiliationType>> affiliationTypes = this.<com.ns.solve.domain.vo.AffiliationType, EnumPath<com.ns.solve.domain.vo.AffiliationType>>createSet("affiliationTypes", com.ns.solve.domain.vo.AffiliationType.class, EnumPath.class, PathInits.DIRECT2);

    public final StringPath description = createString("description");

    public final DateTimePath<java.time.LocalDateTime> endTime = createDateTime("endTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> maxTeamSize = createNumber("maxTeamSize", Integer.class);

    public final StringPath organizerName = createString("organizerName");

    public final SetPath<com.ns.solve.domain.entity.user.User, com.ns.solve.domain.entity.user.QUser> organizers = this.<com.ns.solve.domain.entity.user.User, com.ns.solve.domain.entity.user.QUser>createSet("organizers", com.ns.solve.domain.entity.user.User.class, com.ns.solve.domain.entity.user.QUser.class, PathInits.DIRECT2);

    public final SetPath<com.ns.solve.domain.entity.user.User, com.ns.solve.domain.entity.user.QUser> participants = this.<com.ns.solve.domain.entity.user.User, com.ns.solve.domain.entity.user.QUser>createSet("participants", com.ns.solve.domain.entity.user.User.class, com.ns.solve.domain.entity.user.QUser.class, PathInits.DIRECT2);

    public final StringPath prize = createString("prize");

    public final ListPath<Prize, QPrize> prizes = this.<Prize, QPrize>createList("prizes", Prize.class, QPrize.class, PathInits.DIRECT2);

    public final ListPath<ContestProblem, QContestProblem> problems = this.<ContestProblem, QContestProblem>createList("problems", ContestProblem.class, QContestProblem.class, PathInits.DIRECT2);

    public final BooleanPath reviewConsent = createBoolean("reviewConsent");

    public final StringPath rules = createString("rules");

    public final DateTimePath<java.time.LocalDateTime> startTime = createDateTime("startTime", java.time.LocalDateTime.class);

    public final EnumPath<com.ns.solve.domain.vo.ContestStatus> status = createEnum("status", com.ns.solve.domain.vo.ContestStatus.class);

    public final StringPath title = createString("title");

    public final EnumPath<com.ns.solve.domain.vo.ContestType> type = createEnum("type", com.ns.solve.domain.vo.ContestType.class);

    public QContest(String variable) {
        super(Contest.class, forVariable(variable));
    }

    public QContest(Path<? extends Contest> path) {
        super(path.getType(), path.getMetadata());
    }

    public QContest(PathMetadata metadata) {
        super(Contest.class, metadata);
    }

}


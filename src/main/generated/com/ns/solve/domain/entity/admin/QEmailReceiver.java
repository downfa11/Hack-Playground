package com.ns.solve.domain.entity.admin;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEmailReceiver is a Querydsl query type for EmailReceiver
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmailReceiver extends EntityPathBase<EmailReceiver> {

    private static final long serialVersionUID = -82607232L;

    public static final QEmailReceiver emailReceiver = new QEmailReceiver("emailReceiver");

    public final BooleanPath active = createBoolean("active");

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QEmailReceiver(String variable) {
        super(EmailReceiver.class, forVariable(variable));
    }

    public QEmailReceiver(Path<? extends EmailReceiver> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEmailReceiver(PathMetadata metadata) {
        super(EmailReceiver.class, metadata);
    }

}


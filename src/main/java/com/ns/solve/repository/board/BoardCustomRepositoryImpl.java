package com.ns.solve.repository.board;

import com.ns.solve.domain.entity.BoardType;
import com.ns.solve.domain.entity.QBoard;
import com.ns.solve.domain.entity.QComment;
import com.ns.solve.domain.entity.QUser;
import com.ns.solve.domain.dto.board.BoardSummary;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class BoardCustomRepositoryImpl implements BoardCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final QBoard qBoard;
    private final QComment qComment;
    private final QUser qUser;

    public BoardCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory){
        this.jpaQueryFactory = jpaQueryFactory;
        this.qBoard = QBoard.board;
        this.qComment = QComment.comment;
        this.qUser = QUser.user;
    }

    @Override
    public Page<BoardSummary> findBoardsByPage(BoardType type, Pageable pageable, boolean desc) {
        List<Long> boardIds = jpaQueryFactory
                .select(qBoard.id)
                .from(qBoard)
                .where(qBoard.type.eq(type))
                .orderBy(desc ? qBoard.createdAt.desc() : qBoard.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (boardIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        QComment subComment = new QComment("subComment");

        List<BoardSummary> boardSummaries = jpaQueryFactory
                .select(Projections.constructor(
                        BoardSummary.class,
                        qBoard.id,
                        qBoard.title,
                        qBoard.creator.nickname,
                        qBoard.updatedAt,
                        JPAExpressions
                                .select(subComment.count())
                                .from(subComment)
                                .where(subComment.board.id.eq(qBoard.id))
                ))
                .from(qBoard)
                .join(qBoard.creator, qUser)
                .where(qBoard.id.in(boardIds))
                .orderBy(desc ? qBoard.createdAt.desc() : qBoard.createdAt.asc())
                .fetch();

        long total = jpaQueryFactory
                .selectFrom(qBoard)
                .fetchCount();

        return new PageImpl<>(boardSummaries, pageable, total);
    }



}

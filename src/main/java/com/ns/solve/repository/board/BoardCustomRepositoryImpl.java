package com.ns.solve.repository.board;

import com.ns.solve.domain.QBoard;
import com.ns.solve.domain.QComment;
import com.ns.solve.domain.dto.BoardSummary;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BoardCustomRepositoryImpl implements BoardCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final QBoard qBoard;
    private final QComment qComment;

    public BoardCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory){
        this.jpaQueryFactory = jpaQueryFactory;
        this.qBoard = QBoard.board;
        this.qComment = QComment.comment;
    }


    @Override
    public Page<BoardSummary> findBoardsByPage(Pageable pageable, boolean desc) {
        List<BoardSummary> boardSummaries = jpaQueryFactory
                .selectFrom(qBoard)
                .orderBy(desc ? qBoard.createdAt.desc() : qBoard.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .stream()
                .map(board -> new BoardSummary(
                        board.getId(),
                        board.getTitle(),
                        board.getCreator().getNickname(),
                        board.getUpdatedAt(),
                        board.getCommentList().size())
                )
                .toList();

        long total = jpaQueryFactory
                .selectFrom(qBoard)
                .fetchCount();

        return new PageImpl<>(boardSummaries, pageable, total);
    }

//    @Override
//    public Page<BoardSummary> findBoardsByPage(Pageable pageable, boolean desc) {
//        QComment comment = QComment.comment;
//
//        List<BoardSummary> boardSummaries = jpaQueryFactory
//                .select(qBoard.id, qBoard.title, qBoard.creator.nickname, qBoard.updatedAt,
//                        qComment.count())
//                .from(qBoard)
//                .leftJoin(qBoard.commentList, qComment)
//                .groupBy(qBoard.id)
//                .orderBy(desc ? qBoard.createdAt.desc() : qBoard.createdAt.asc())
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch()
//                .stream()
//                .map(result -> new BoardSummary(
//                        result.get(qBoard.id),
//                        result.get(qBoard.title),
//                        result.get(qBoard.creator.nickname),
//                        result.get(qBoard.updatedAt),
//                        result.get(qComment.count()).intValue())
//                )
//                .toList();
//
//        long total = jpaQueryFactory
//                .selectFrom(qBoard)
//                .fetchCount();
//
//        return new PageImpl<>(boardSummaries, pageable, total);
//    }


}

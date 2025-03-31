package com.ns.solve.repository.problem;

import com.ns.solve.domain.QComment;
import com.ns.solve.domain.dto.problem.ProblemSummary;
import com.ns.solve.domain.problem.Problem;
import com.ns.solve.domain.problem.QProblem;
import com.ns.solve.domain.problem.QWargameProblem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class ProblemCustomRepositoryImpl implements ProblemCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final QWargameProblem qWargameProblem;
    private final QProblem qProblem;
    private final QComment qComment;

    public ProblemCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory){
        this.jpaQueryFactory = jpaQueryFactory;
        this.qWargameProblem = QWargameProblem.wargameProblem;
        this.qProblem = QProblem.problem;
        this.qComment = QComment.comment;
    }

    // status가 '검수 전'인 Problem의 전체 목록을 조회하는 메서드 (Pagenation)
    @Override
    public Page<Problem> findProblemsByStatusPending(PageRequest pageRequest) {
        // 검수전에는 댓글을 달 수 없으므로, 불필요한 쿼리문이 추가될 수 없다
        List<Problem> results = jpaQueryFactory
                .selectFrom(qProblem)
                .where(qProblem.isChecked.isFalse())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        long total = jpaQueryFactory
                .selectFrom(qProblem)
                .where(qProblem.isChecked.isFalse())
                .fetchCount();

        return new PageImpl<>(results, pageRequest, total);
    }

    @Override
    public Boolean matchFlagToProblems(Long problemId, String attemptedFlag ) {
        String correctFlag  = jpaQueryFactory.select(qWargameProblem.flag)
                .from(qWargameProblem)
                .where(qWargameProblem.id.eq(problemId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne();

        return Objects.equals(attemptedFlag , correctFlag );
    }


    // status가 '검수 완료'인 Problem을 type과 상속된 데이터에 맞게 리스트를 조회하는 메서드 (Pagenation)
    @Override
    public Page<ProblemSummary> findProblemsByStatusAndTypeSortedById(String type, boolean desc, PageRequest pageRequest) {
        return findProblemsSorted(type, pageRequest, qProblem.id, desc);
    }

    @Override
    public Page<ProblemSummary> findProblemsByStatusAndTypeSortedByUpdatedAt(String type, boolean desc, PageRequest pageRequest) {
        return findProblemsSorted(type, pageRequest, qProblem.updatedAt, desc);
    }

    @Override
    public Page<ProblemSummary> findProblemsByStatusAndTypeSortedByCorrectRate(String type, boolean desc, PageRequest pageRequest) {
        return findProblemsSorted(type, pageRequest, qProblem.correctCount.divide(qProblem.entireCount), desc);
    }


    // 일단 WargameProblem을 조회한다. 나중에 알고리즘이나 다른 종류의 문제가 추가된다면 다시 설정하겠음
    private <T extends Comparable<?>> Page<ProblemSummary> findProblemsSorted(String type, PageRequest pageRequest, com.querydsl.core.types.dsl.ComparableExpressionBase<T> sortField, boolean desc) {
        List<ProblemSummary> results = jpaQueryFactory
                .selectFrom(qWargameProblem)
                .leftJoin(qWargameProblem.commentList, qComment)
                .where(qWargameProblem.isChecked.isTrue().and(typeEq(type)))
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .orderBy(desc ? sortField.desc() : sortField.asc())
                .fetch()
                .stream()
                .map(problem -> new ProblemSummary(
                        problem.getId(),
                        null,
                        problem.getTitle(),
                        problem.getLevel(),
                        problem.getCorrectCount() / (double) problem.getEntireCount(),
                        problem.getCreator(),
                        problem.getKind(),
                        problem.getUpdatedAt()
                ))
                .toList();

        long total = jpaQueryFactory
                .selectFrom(qWargameProblem)
                .where(qWargameProblem.isChecked.isTrue().and(typeEq(type)))
                .fetchCount();

        return new PageImpl<>(results, pageRequest, total);
    }

    private BooleanExpression typeEq(String type) {
        if (type == null) {
            return null;
        }

        try {
            return qWargameProblem.type.stringValue().eq(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


}

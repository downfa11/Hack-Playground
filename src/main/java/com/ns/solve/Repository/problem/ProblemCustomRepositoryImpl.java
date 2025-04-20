package com.ns.solve.repository.problem;

import com.ns.solve.domain.dto.problem.ProblemSummary;
import com.ns.solve.domain.dto.problem.QProblemSummary;
import com.ns.solve.domain.entity.QComment;
import com.ns.solve.domain.entity.problem.*;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
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

    public ProblemCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
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
    public Boolean matchFlagToProblems(Long problemId, String attemptedFlag) {
        String correctFlag = jpaQueryFactory.select(qWargameProblem.flag)
                .from(qWargameProblem)
                .where(qWargameProblem.id.eq(problemId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne();

        return Objects.equals(attemptedFlag, correctFlag);
    }

    @Override
    public List<WargameProblem> findByTypeWargame(ProblemType wargameType) {
        return jpaQueryFactory.selectFrom(qWargameProblem)
                .where(qWargameProblem.type.eq(wargameType))
                .fetch();
    }

    @Override
    public Problem findProblemWithLock(Long problemId) {
        return jpaQueryFactory.selectFrom(qProblem)
                .where(qProblem.id.eq(problemId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne();
    }

    @Override
    public long countCheckedProblems() {
        Long count = jpaQueryFactory
                .select(Wildcard.count)
                .from(qProblem)
                .where(qProblem.isChecked.isTrue())
                .fetchOne();

        return count != null ? count : 0L;
    }

    @Override
    public long countNewProblems(LocalDateTime now) {
        Long count = jpaQueryFactory
                .select(Wildcard.count)
                .from(qProblem)
                .where(qProblem.isChecked.isTrue()
                                .and(qProblem.createdAt.goe(now.minusMonths(1))))
                .fetchOne();

        return count != null ? count : 0L;
    }


    // status가 '검수 완료'인 Problem을 type과 상속된 데이터에 맞게 리스트를 조회하는 메서드 (Pagenation)
    @Override
    public Page<ProblemSummary> findProblemsByStatusAndTypeSortedById(ProblemType type, String kind, boolean desc, PageRequest pageRequest) {
        return findProblemsSorted(type, kind, pageRequest, qProblem.id, desc);
    }

    @Override
    public Page<ProblemSummary> findProblemsByStatusAndTypeSortedByUpdatedAt(ProblemType type, String kind, boolean desc, PageRequest pageRequest) {
        return findProblemsSorted(type, kind, pageRequest, qProblem.updatedAt, desc);
    }

    @Override
    public Page<ProblemSummary> findProblemsByStatusAndTypeSortedByCorrectRate(ProblemType type, String kind, boolean desc, PageRequest pageRequest) {
        return findProblemsSorted(type, kind, pageRequest, qProblem.correctCount.divide(qProblem.entireCount), desc);
    }

    private <T extends Comparable<?>> Page<ProblemSummary> findProblemsSorted(ProblemType type, String kind, PageRequest pageRequest, com.querydsl.core.types.dsl.ComparableExpressionBase<T> sortField, boolean desc) {
        BooleanExpression condition = qProblem.isChecked.isTrue().and(typeEq(type));
        BooleanExpression kindCondition = kindEq(kind);

        if (kindCondition != null) {
            condition = condition.and(kindCondition);
        }

        OrderSpecifier<T> orderSpecifier = desc ? sortField.desc() : sortField.asc();

        List<ProblemSummary> results = jpaQueryFactory
                .select(new QProblemSummary(
                        qProblem.id,
                        qProblem.title,
                        qWargameProblem.level,
                        qProblem.correctCount.divide(qProblem.entireCount.coalesce(0.0)),
                        qProblem.creator.nickname,
                        qWargameProblem.kind,
                        qProblem.updatedAt
                ))
                .from(qProblem)
                .leftJoin(qWargameProblem).on(qProblem.id.eq(qWargameProblem.id))
                .where(condition)
                .orderBy(orderSpecifier)
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();


        long total = jpaQueryFactory
                .selectFrom(qProblem)
                .leftJoin(qWargameProblem).on(qProblem.id.eq(qWargameProblem.id))
                .where(condition)
                .fetchCount();

        return new PageImpl<>(results, pageRequest, total);
    }

    private BooleanExpression typeEq(ProblemType type) {
        if (type == null) {
            return null;
        }
        try {
            return qWargameProblem.type.eq(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private BooleanExpression kindEq(String kind) {
        if (kind == null || kind.isBlank()) {
            return null;
        }
        return qWargameProblem.kind.eq(kind);
    }
}

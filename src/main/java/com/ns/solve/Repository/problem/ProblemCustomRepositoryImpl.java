package com.ns.solve.repository.problem;

import com.ns.solve.domain.dto.problem.ProblemSummary;
import com.ns.solve.domain.dto.problem.QProblemSummary;
import com.ns.solve.domain.entity.problem.*;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class ProblemCustomRepositoryImpl implements ProblemCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final QWargameProblem qWargameProblem;
    private final QProblem qProblem;

    public ProblemCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
        this.qWargameProblem = QWargameProblem.wargameProblem;
        this.qProblem = QProblem.problem;
    }

    // Todo. 현재 조회는 QWargameProblem 이라고 가정한 상황임. 추후 확장된다면 개선해야한다.
    @Override
    public Page<ProblemSummary> searchKeywordInTitle(ProblemType type, WargameKind kind, String keyword, Pageable pageable) {
        boolean desc = pageable.getSort().stream()
                .anyMatch(order -> order.getProperty().equals("createdAt") && order.isDescending());

        BooleanExpression condition = qProblem.type.eq(type)
                .and(qProblem.title.containsIgnoreCase(keyword));

        NumberExpression<Double> correctRatePercent = qProblem.correctCount.doubleValue()
                .multiply(100.0)
                .divide(qProblem.entireCount.coalesce(1.0).doubleValue());

        if (kind != null) {
            condition = condition.and(qWargameProblem.kind.eq(kind));
        }

        List<Long> boardIds = jpaQueryFactory
                .select(qProblem.id)
                .from(qProblem)
                .leftJoin(qWargameProblem).on(qProblem.id.eq(qWargameProblem.id))
                .where(condition)
                .orderBy(desc ? qProblem.createdAt.desc() : qProblem.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (boardIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<ProblemSummary> results = jpaQueryFactory
                .select(new QProblemSummary(
                        qProblem.id,
                        qProblem.title,
                        qWargameProblem.level,
                        correctRatePercent,
                        qProblem.creator.nickname,
                        qProblem.type.stringValue(),
                        qWargameProblem.kind.stringValue(),
                        qProblem.updatedAt
                ))
                .from(qProblem)
                .leftJoin(qWargameProblem).on(qProblem.id.eq(qWargameProblem.id))
                .where(condition)
                .orderBy(qProblem.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory
                .select(qProblem.count())
                .from(qProblem)
                .leftJoin(qWargameProblem).on(qProblem.id.eq(qWargameProblem.id))
                .where(condition)
                .fetchOne();

        return new PageImpl<>(results, pageable, total != null ? total : 0);
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
    public Boolean matchFlagToWargameProblem(Long problemId, String attemptedFlag) {
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
    public Page<ProblemSummary> findProblemsByStatusAndTypeSortedById(ProblemType type, WargameKind kind, boolean desc, PageRequest pageRequest) {
        return findProblemsSorted(type, kind, pageRequest, qProblem.id, desc);
    }

    @Override
    public Page<ProblemSummary> findProblemsByStatusAndTypeSortedByUpdatedAt(ProblemType type, WargameKind kind, boolean desc, PageRequest pageRequest) {
        return findProblemsSorted(type, kind, pageRequest, qProblem.updatedAt, desc);
    }

    @Override
    public Page<ProblemSummary> findProblemsByStatusAndTypeSortedByCorrectRate(ProblemType type, WargameKind kind, boolean desc, PageRequest pageRequest) {
        return findProblemsSorted(type, kind, pageRequest, qProblem.correctCount.doubleValue().divide(qProblem.entireCount.doubleValue()), desc);
    }

    // Todo. 현재 조회는 QWargameProblem 이라고 가정한 상황임. 추후 확장된다면 개선해야한다.
    private <T extends Comparable<?>> Page<ProblemSummary> findProblemsSorted(ProblemType type, WargameKind kind, PageRequest pageRequest, com.querydsl.core.types.dsl.ComparableExpressionBase<T> sortField, boolean desc) {
        BooleanExpression condition = qProblem.isChecked.isTrue().and(typeEq(type));
        BooleanExpression kindCondition = kindEq(kind);

        if (kindCondition != null) {
            condition = condition.and(kindCondition);
        }

        OrderSpecifier<T> orderSpecifier = desc ? sortField.desc() : sortField.asc();
        NumberExpression<Double> correctRatePercent = qProblem.correctCount.doubleValue()
                .multiply(100.0)
                .divide(qProblem.entireCount.coalesce(1.0).doubleValue());

        List<ProblemSummary> results = jpaQueryFactory
                .select(new QProblemSummary(
                        qProblem.id,
                        qProblem.title,
                        qWargameProblem.level,
                        correctRatePercent,
                        qProblem.creator.nickname,
                        qProblem.type.stringValue(),
                        qWargameProblem.kind.stringValue(),
                        qProblem.updatedAt
                ))
                .from(qProblem)
                .leftJoin(qWargameProblem).on(qProblem.id.eq(qWargameProblem.id))
                .where(condition)
                .orderBy(orderSpecifier)
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();


        Long total = jpaQueryFactory
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

    private BooleanExpression kindEq(WargameKind kind) {
        if (kind == null) {
            return null;
        }
        return qWargameProblem.kind.eq(kind);
    }
}

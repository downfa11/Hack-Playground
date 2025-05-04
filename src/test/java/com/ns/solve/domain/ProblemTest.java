package com.ns.solve.domain;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ns.solve.domain.entity.User;
import com.ns.solve.domain.entity.problem.Problem;
import java.time.LocalDateTime;

import com.ns.solve.domain.entity.problem.ProblemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProblemTest {
    private Problem problem;

    @BeforeEach
    void setUp() {
        problem = new Problem();
        problem.setTitle("title");
        problem.setIsChecked(false);
        problem.setType(ProblemType.WARGAME);
        problem.setCreator(new User());
        problem.setEntireCount(0.0);
        problem.setCorrectCount(0.0);
    }

    @Test
    void 문제의_전체_시도횟수_증가하는_메서드() {
        // given
        problem.incrementEntireCount();

        // when
        double result = problem.getEntireCount();

        // then
        assertEquals(1.0, result, 0.01);
    }

    @Test
    void 문제의_전체_정답수_증가하는_메서드() {
        // given
        problem.incrementCorrectCount();

        // when
        double result = problem.getCorrectCount();

        // then
        assertEquals(1.0, result, 0.01);
    }

    @Test
    void 문제_생성시_날짜_검증() {
        // given
        problem.prePersist();

        // when
        LocalDateTime createdAt = problem.getCreatedAt();
        LocalDateTime updatedAt = problem.getUpdatedAt();

        // then
        assertNotNull(createdAt);
        assertNotNull(updatedAt);
        assertEquals(createdAt, updatedAt);
    }

    @Test
    void 문제_수정시_날짜_검증() throws InterruptedException {
        // given
        problem.prePersist();
        LocalDateTime updatedAtBefore = problem.getUpdatedAt();

        // when
        Thread.sleep(1000);
        problem.preUpdate();
        LocalDateTime updatedAtAfter = problem.getUpdatedAt();

        // then
        assertNotNull(updatedAtBefore);
        assertNotNull(updatedAtAfter);
        assertNotEquals(updatedAtBefore, updatedAtAfter);
    }
}


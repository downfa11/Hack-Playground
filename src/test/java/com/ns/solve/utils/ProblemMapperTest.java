package com.ns.solve.utils;

import com.ns.solve.domain.dto.problem.ProblemSummary;
import com.ns.solve.domain.entity.user.User;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.domain.entity.problem.ProblemType;
import com.ns.solve.domain.entity.problem.WargameProblem;
import java.util.List;

import com.ns.solve.utils.mapper.ProblemMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProblemMapperTest {

    @Test
    void testToProblemSummary() {
        // given
        Problem problem = mock(Problem.class);
        when(problem.getId()).thenReturn(1L);
        when(problem.getTitle()).thenReturn("title");
        when(problem.getCreator()).thenReturn(new User());
        when(problem.getType()).thenReturn(ProblemType.WARGAME);
        when(problem.getCorrectCount()).thenReturn(80.0);
        when(problem.getEntireCount()).thenReturn(100.0);

        // when
        ProblemSummary summary = ProblemMapper.mapperToProblemSummary(problem);

        // then
        assertNotNull(summary);
        assertEquals(1L, summary.getId());
        assertEquals("title", summary.getTitle());
        assertEquals("creator", summary.getCreator());
        assertEquals("type", summary.getType());
        assertEquals(0.8, summary.getCorrectRate(), 0.01);
    }

    @Test
    void WargameProblem_Mapping() {
        // given
        WargameProblem wargameProblem = mock(WargameProblem.class);
        when(wargameProblem.getId()).thenReturn(1L);
        when(wargameProblem.getTitle()).thenReturn("title");
        when(wargameProblem.getCreator()).thenReturn(new User());
        when(wargameProblem.getType()).thenReturn(ProblemType.WARGAME);
        when(wargameProblem.getCorrectCount()).thenReturn(75.0);
        when(wargameProblem.getEntireCount()).thenReturn(100.0);
        when(wargameProblem.getLevel()).thenReturn("hard");

        // when
        ProblemSummary summary = ProblemMapper.mapperToWargameProblemSummary(wargameProblem);

        // then
        assertNotNull(summary);
        assertEquals(1L, summary.getId());
        assertEquals("title", summary.getTitle());
        assertEquals("creator", summary.getCreator());
        assertEquals("type", summary.getType());
        assertEquals(0.75, summary.getCorrectRate(), 0.01);
        assertEquals("hard", summary.getLevel());
    }

    @Test
    void Problem와_WargameProblem이_동시에_존재하는_경우() {
        // given
        Problem problem1 = mock(Problem.class);
        when(problem1.getId()).thenReturn(1L);
        when(problem1.getTitle()).thenReturn("title1");
        when(problem1.getCreator()).thenReturn(new User());
        when(problem1.getType()).thenReturn(ProblemType.WARGAME);
        when(problem1.getCorrectCount()).thenReturn(90.0);
        when(problem1.getEntireCount()).thenReturn(100.0);

        WargameProblem problem2 = mock(WargameProblem.class);
        when(problem2.getId()).thenReturn(2L);
        when(problem2.getTitle()).thenReturn("title2");
        when(problem2.getCreator()).thenReturn(new User());
        when(problem2.getType()).thenReturn(ProblemType.ALGORITHM);
        when(problem2.getCorrectCount()).thenReturn(85.0);
        when(problem2.getEntireCount()).thenReturn(100.0);
        when(problem2.getLevel()).thenReturn("hard");

        List<Problem> problems = List.of(problem1, problem2);

        // when
        List<ProblemSummary> summaries = ProblemMapper.mapperToProblemSummaryList(problems);

        // then
        assertNotNull(summaries);
        assertEquals(2, summaries.size());

        ProblemSummary summary1 = summaries.get(0);
        assertEquals(1L, summary1.getId());
        assertEquals("title1", summary1.getTitle());
        assertEquals(ProblemType.WARGAME, summary1.getCreator());
        assertEquals("type1", summary1.getType());
        assertEquals(0.9, summary1.getCorrectRate(), 0.01);

        ProblemSummary summary2 = summaries.get(1);
        assertEquals(2L, summary2.getId());
        assertEquals("title2", summary2.getTitle());
        assertEquals("creator2", summary2.getCreator());
        assertEquals(ProblemType.ALGORITHM, summary2.getType());
        assertEquals(0.85, summary2.getCorrectRate(), 0.01);
        assertEquals("hard", summary2.getLevel());
    }
}


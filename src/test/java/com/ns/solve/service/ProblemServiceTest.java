package com.ns.solve.service;

import com.ns.solve.domain.Solved;
import com.ns.solve.domain.User;
import com.ns.solve.domain.dto.problem.ModifyProblemDto;
import com.ns.solve.domain.dto.problem.RegisterProblemDto;
import com.ns.solve.domain.dto.UserDto;
import com.ns.solve.domain.dto.problem.wargame.WargameProblemDto;
import com.ns.solve.domain.problem.Problem;
import com.ns.solve.domain.problem.ProblemType;
import com.ns.solve.domain.problem.WargameProblem;
import com.ns.solve.repository.SolvedRepository;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.repository.problem.ProblemRepository;
import com.ns.solve.service.problem.ProblemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock private ProblemRepository problemRepository;
    @Mock private UserRepository userRepository;
    @Mock private SolvedRepository solvedRepository;

    @InjectMocks private ProblemService problemService;

    private Problem sampleProblem;
    private WargameProblem wargameProblem;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleProblem = new Problem();
        sampleProblem.setId(1L);
        sampleProblem.setTitle("Problem");
        sampleProblem.setIsChecked(false);

        wargameProblem = new WargameProblem();
        wargameProblem.setId(2L);
        wargameProblem.setTitle("Wargame");
        wargameProblem.setIsChecked(false);

        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setNickname("FirstBlood");
        sampleUser.setScore(100L);
        sampleUser.setLastActived(LocalDateTime.now());
    }

    @Test
    void testCreateProblem() {
        RegisterProblemDto dto = RegisterProblemDto.builder()
                .title("Problem")
                .type(ProblemType.WARGAME)
                .creator("creator")
                .detail("detail")
                .tags(List.of("tag1", "tag2"))
                .build();

        Problem expectedProblem = new Problem();
        expectedProblem.setTitle(dto.getTitle());
        expectedProblem.setType(dto.getType());
        expectedProblem.setCreator(dto.getCreator());
        expectedProblem.setDetail(dto.getDetail());
        expectedProblem.setTags(dto.getTags());

        when(problemRepository.save(any(Problem.class))).thenReturn(expectedProblem);

        Problem created = problemService.createProblem(dto);

        assertNotNull(created);
        assertEquals(dto.getTitle(), created.getTitle());
        assertEquals(dto.getType(), created.getType());
        assertEquals(dto.getCreator(), created.getCreator());
        assertEquals(dto.getDetail(), created.getDetail());
        assertEquals(dto.getTags(), created.getTags());

        verify(problemRepository, times(1)).save(any(Problem.class));
    }



    @Test
    void testUpdateProblem() {
        ModifyProblemDto dto = ModifyProblemDto.builder()
                .title("Problem")
                .type(ProblemType.WARGAME)
                .creator("creator")
                .detail("detail")
                .tags(List.of("tag1", "tag2"))
                .build();

        Problem existingProblem = new Problem();
        existingProblem.setId(1L);
        existingProblem.setTitle("Old Title");
        existingProblem.setType(ProblemType.WARGAME);
        existingProblem.setCreator("Old Creator");
        existingProblem.setDetail("Old Detail");
        existingProblem.setTags(List.of("oldTag"));

        when(problemRepository.findById(1L)).thenReturn(Optional.of(existingProblem));
        when(problemRepository.save(any(Problem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Problem result = problemService.updateProblem(dto);

        assertNotNull(result);
        assertEquals(dto.getTitle(), result.getTitle());
        assertEquals(dto.getType(), result.getType());
        assertEquals(dto.getCreator(), result.getCreator());
        assertEquals(dto.getDetail(), result.getDetail());
        assertEquals(dto.getTags(), result.getTags());

        verify(problemRepository, times(1)).findById(1L);
        verify(problemRepository, times(1)).save(any(Problem.class));
    }


    @Test
    void testDeleteProblem() {
        doNothing().when(problemRepository).deleteById(1L);

        problemService.deleteProblem(1L);

        verify(problemRepository, times(1)).deleteById(1L);
    }

    @Test
    void testGetProblemById() {
        when(problemRepository.findById(1L)).thenReturn(Optional.of(sampleProblem));

        Optional<Problem> result = problemService.getProblemById(1L);

        assertTrue(result.isPresent());
        assertEquals(sampleProblem.getTitle(), result.get().getTitle());
    }

    @Test
    void testToggleProblemCheckStatus() {
        when(problemRepository.findById(1L)).thenReturn(Optional.of(wargameProblem));
        when(problemRepository.save(any(Problem.class))).thenReturn(wargameProblem);

        WargameProblemDto result = problemService.toggleProblemCheckStatus(1L);

        assertNotNull(result);
        assertEquals(wargameProblem.getTitle(), result.getTitle());
        verify(problemRepository, times(1)).save(wargameProblem);
    }

    @Test
    void testFirstBlood() {
        int firstCount = 1;
        User sampleUser = new User();
        sampleUser.setNickname("testUser");
        sampleUser.setScore(100L);
        sampleUser.setLastActived(LocalDateTime.now());

        Pageable pageable = PageRequest.of(0, firstCount);
        Page<User> page = new PageImpl<>(List.of(sampleUser), pageable, 1);

        when(solvedRepository.findFirstUserToSolveProblem(1L, pageable)).thenReturn(page);

        Optional<UserDto> result = problemService.firstBlood(1L, firstCount);

        assertTrue(result.isPresent());
        assertEquals(sampleUser.getNickname(), result.get().nickname());
    }


    @Test
    void testSolveProblem_CorrectFlag() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(problemRepository.findProblemWithLock(1L)).thenReturn(sampleProblem);
        when(problemRepository.matchFlagToProblems(1L, "correct")).thenReturn(true);
        when(solvedRepository.save(any(Solved.class))).thenReturn(new Solved());
        when(problemRepository.save(any(Problem.class))).thenReturn(sampleProblem);

        boolean result = problemService.solveProblem(1L, 1L, "correct");

        assertTrue(result);
        verify(solvedRepository, times(1)).save(any(Solved.class));
    }

    @Test
    void testSolveProblem_WrongFlag() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(problemRepository.findProblemWithLock(1L)).thenReturn(sampleProblem);
        when(problemRepository.matchFlagToProblems(1L, "wrong")).thenReturn(false);
        when(problemRepository.save(any(Problem.class))).thenReturn(sampleProblem);

        boolean result = problemService.solveProblem(1L, 1L, "wrong");

        assertFalse(result);
        verify(solvedRepository, times(1)).save(any(Solved.class));
    }
}

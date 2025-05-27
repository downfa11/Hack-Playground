package com.ns.solve.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ns.solve.domain.dto.comment.CommentDto;
import com.ns.solve.domain.entity.Board;
import com.ns.solve.domain.entity.Comment;
import com.ns.solve.domain.entity.CommentType;
import com.ns.solve.domain.entity.User;
import com.ns.solve.domain.dto.comment.ModifyCommentDto;
import com.ns.solve.domain.dto.comment.RegisterCommentDto;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.domain.entity.problem.ProblemType;
import com.ns.solve.repository.CommentRepository;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.repository.board.BoardRepository;
import com.ns.solve.repository.problem.ProblemRepository;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private CommentService commentService;

    private Comment testComment;
    private User testUser;
    private Problem testProblem;
    private Board testBoard;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testProblem = new Problem();
        testProblem.setId(1L);

        testBoard = new Board();
        testBoard.setId(1L);

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setContent("Test Comment");
        testComment.setType(CommentType.BOARD);
        testComment.setCreator(testUser);
        testComment.setProblem(testProblem);
    }

    @Test
    void testCreateComment() {
        RegisterCommentDto dto = new RegisterCommentDto(CommentType.BOARD, 1L, "Test Comment");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(problemRepository.findById(1L)).thenReturn(Optional.of(testProblem));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        CommentDto createdComment = commentService.createComment(1L, dto);

        assertNotNull(createdComment);
        assertEquals("Test Comment", createdComment.getContents());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testGetAllComments() {
        when(commentRepository.findAll()).thenReturn(List.of(testComment));

        List<CommentDto> comments = commentService.getAllComments();

        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());
        assertEquals("Test Comment", comments.get(0).getContents());
        verify(commentRepository, times(1)).findAll();
    }

    @Test
    void testGetCommentsByProblemId() {
        when(commentRepository.findByProblemId(1L)).thenReturn(List.of(testComment));

        List<CommentDto> comments = commentService.getCommentsByProblemId(1L);

        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());
        verify(commentRepository, times(1)).findByProblemId(1L);
    }

    @Test
    void testGetCommentsByBoardId() {
        when(commentRepository.findByBoardId(1L)).thenReturn(List.of(testComment));

        List<CommentDto> comments = commentService.getCommentsByBoardId(1L);

        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());
        verify(commentRepository, times(1)).findByBoardId(1L);
    }

    @Test
    void testGetCommentById() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        Optional<CommentDto> comment = commentService.getCommentById(1L);

        assertTrue(comment.isPresent());
        assertEquals("Test Comment", comment.get().getContents());
        verify(commentRepository, times(1)).findById(1L);
    }

    @Test
    void testUpdateComment() {
        ModifyCommentDto dto = new ModifyCommentDto( CommentType.BOARD,"Updated Comment");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        CommentDto updatedComment = commentService.updateComment(1L, 1L, dto);

        assertNotNull(updatedComment);
        assertEquals("Updated Comment", updatedComment.getContents());
        verify(commentRepository, times(1)).findById(1L);
        verify(commentRepository, times(1)).save(testComment);
    }

    @Test
    void testDeleteComment() {
        doNothing().when(commentRepository).deleteById(1L);

        commentService.deleteComment(1L, 1L);

        verify(commentRepository, times(1)).deleteById(1L);
    }
}

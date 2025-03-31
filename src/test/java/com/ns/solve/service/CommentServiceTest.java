package com.ns.solve.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ns.solve.domain.Board;
import com.ns.solve.domain.Comment;
import com.ns.solve.domain.User;
import com.ns.solve.domain.dto.ModifyCommentDto;
import com.ns.solve.domain.dto.RegisterCommentDto;
import com.ns.solve.domain.problem.Problem;
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
        testComment.setType("problem");
        testComment.setCreator(testUser);
        testComment.setProblem(testProblem);
    }

    @Test
    void testCreateComment() {
        RegisterCommentDto dto = new RegisterCommentDto("problem", 1L, 1L, "Test Comment");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(problemRepository.findById(1L)).thenReturn(Optional.of(testProblem));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        Comment createdComment = commentService.createComment(dto);

        assertNotNull(createdComment);
        assertEquals("Test Comment", createdComment.getContent());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testGetAllComments() {
        when(commentRepository.findAll()).thenReturn(List.of(testComment));

        List<Comment> comments = commentService.getAllComments();

        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());
        assertEquals("Test Comment", comments.get(0).getContent());
        verify(commentRepository, times(1)).findAll();
    }

    @Test
    void testGetCommentsByProblemId() {
        when(commentRepository.findByProblemId(1L)).thenReturn(List.of(testComment));

        List<Comment> comments = commentService.getCommentsByProblemId(1L);

        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());
        verify(commentRepository, times(1)).findByProblemId(1L);
    }

    @Test
    void testGetCommentsByBoardId() {
        when(commentRepository.findByBoardId(1L)).thenReturn(List.of(testComment));

        List<Comment> comments = commentService.getCommentsByBoardId(1L);

        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());
        verify(commentRepository, times(1)).findByBoardId(1L);
    }

    @Test
    void testGetCommentById() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        Optional<Comment> comment = commentService.getCommentById(1L);

        assertTrue(comment.isPresent());
        assertEquals("Test Comment", comment.get().getContent());
        verify(commentRepository, times(1)).findById(1L);
    }

    @Test
    void testUpdateComment() {
        ModifyCommentDto dto = new ModifyCommentDto(1L, "free","Updated Comment");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        Comment updatedComment = commentService.updateComment(dto);

        assertNotNull(updatedComment);
        assertEquals("Updated Comment", updatedComment.getContent());
        verify(commentRepository, times(1)).findById(1L);
        verify(commentRepository, times(1)).save(testComment);
    }

    @Test
    void testDeleteComment() {
        doNothing().when(commentRepository).deleteById(1L);

        commentService.deleteComment(1L);

        verify(commentRepository, times(1)).deleteById(1L);
    }
}

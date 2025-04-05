package com.ns.solve.service;

import com.ns.solve.domain.Board;
import com.ns.solve.domain.Comment;
import com.ns.solve.domain.User;
import com.ns.solve.domain.dto.comment.ModifyCommentDto;
import com.ns.solve.domain.dto.comment.RegisterCommentDto;
import com.ns.solve.domain.problem.Problem;
import com.ns.solve.repository.CommentRepository;
import java.util.List;
import java.util.Optional;

import com.ns.solve.repository.UserRepository;
import com.ns.solve.repository.board.BoardRepository;
import com.ns.solve.repository.problem.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final BoardRepository boardRepository;


    public Comment createComment(RegisterCommentDto registerCommentDto) {
        Comment comment = new Comment();
        comment.setContent(registerCommentDto.contents());
        comment.setType(registerCommentDto.type());

        User creator = userRepository.findById(registerCommentDto.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        comment.setCreator(creator);

        if ("problem".equals(registerCommentDto.type())) {
            Problem problem = problemRepository.findById(registerCommentDto.parentId())
                    .orElseThrow(() -> new RuntimeException("Problem not found"));
            comment.setProblem(problem);
        } else {
            Board board = boardRepository.findById(registerCommentDto.parentId())
                    .orElseThrow(() -> new RuntimeException("Free Board not found"));
            comment.setBoard(board);
        }

        return commentRepository.save(comment);
    }

    public List<Comment> getAllComments() { return commentRepository.findAll(); }
    public List<Comment> getCommentsByProblemId(Long problemId) { return commentRepository.findByProblemId(problemId);}
    public List<Comment> getCommentsByBoardId(Long boardId) {
        return commentRepository.findByBoardId(boardId);
    }
    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }
    public Comment updateComment(ModifyCommentDto modifyCommentDto) {
        Long commentId = modifyCommentDto.commentId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        comment.setType(modifyCommentDto.type());
        comment.setContent(modifyCommentDto.contents());
        return commentRepository.save(comment);
    }
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
}
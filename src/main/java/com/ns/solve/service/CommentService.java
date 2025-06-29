package com.ns.solve.service;

import com.ns.solve.domain.dto.comment.CommentDto;
import com.ns.solve.domain.entity.Board;
import com.ns.solve.domain.entity.Comment;
import com.ns.solve.domain.entity.CommentType;
import com.ns.solve.domain.entity.User;
import com.ns.solve.domain.dto.comment.ModifyCommentDto;
import com.ns.solve.domain.dto.comment.RegisterCommentDto;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.repository.CommentRepository;
import java.util.List;
import java.util.Optional;

import com.ns.solve.repository.UserRepository;
import com.ns.solve.repository.board.BoardRepository;
import com.ns.solve.repository.problem.ProblemRepository;
import com.ns.solve.utils.exception.ErrorCode.BoardErrorCode;
import com.ns.solve.utils.exception.ErrorCode.CommentErrorCode;
import com.ns.solve.utils.exception.ErrorCode.ProblemErrorCode;
import com.ns.solve.utils.exception.ErrorCode.UserErrorCode;
import com.ns.solve.utils.exception.SolvedException;
import com.ns.solve.utils.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ProblemRepository problemRepository;
    private final BoardRepository boardRepository;

    private final UserService userService;

    public CommentDto createComment(Long userId, RegisterCommentDto registerCommentDto) {
        Comment comment = new Comment();
        comment.setContent(registerCommentDto.contents());
        comment.setType(registerCommentDto.type());

        User creator = userService.findByUserId(userId)
                .orElseThrow(() -> new SolvedException(UserErrorCode.USER_NOT_FOUND, "User not found"));
        comment.setCreator(creator);

        if (CommentType.PROBLEM.equals(registerCommentDto.type())) {
            Problem problem = problemRepository.findById(registerCommentDto.parentId())
                    .orElseThrow(() -> new SolvedException(ProblemErrorCode.PROBLEM_NOT_FOUND, "Problem not found"));
            comment.setProblem(problem);

        } else if (CommentType.BOARD.equals(registerCommentDto.type())){
            Board board = boardRepository.findById(registerCommentDto.parentId())
                    .orElseThrow(() -> new SolvedException(BoardErrorCode.BOARD_NOT_FOUND, "Free Board not found"));
            comment.setBoard(board);
        }
        else throw new SolvedException(CommentErrorCode.INVALID_COMMENT_OPERATION, "Invalid Type");

        userService.updateLastActived(creator);
        comment = commentRepository.save(comment);
        return CommentMapper.mapperToCommentDto(comment);
    }

    public List<CommentDto> getAllComments() {
        return commentRepository.findAll()
                .stream().map(CommentMapper::mapperToCommentDto)
                .toList();
    }

    public List<CommentDto> getCommentsByProblemId(Long problemId) {
        return commentRepository.findByProblemId(problemId)
                .stream().map(CommentMapper::mapperToCommentDto)
                .toList();
    }

    public List<CommentDto> getCommentsByBoardId(Long boardId) {

        return commentRepository.findByBoardId(boardId)
                .stream().map(CommentMapper::mapperToCommentDto)
                .toList();
    }
    public Optional<CommentDto> getCommentById(Long id) {

        return commentRepository.findById(id)
                .map(CommentMapper::mapperToCommentDto);
    }

    public CommentDto updateComment(Long userId, Long commentId, ModifyCommentDto modifyCommentDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new SolvedException(CommentErrorCode.COMMENT_NOT_FOUND, "Comment not found"));
        User user = userService.findByUserId(userId)
                .orElseThrow(() -> new SolvedException(CommentErrorCode.COMMENT_NOT_FOUND, "User not found"));

        checkAuthorizationOrThrow(user, comment);
        comment.setType(modifyCommentDto.type());
        comment.setContent(modifyCommentDto.contents());

        userService.updateLastActived(user);
        comment = commentRepository.save(comment);
        return CommentMapper.mapperToCommentDto(comment);

    }
    public void deleteComment(Long userId, Long commentId)
    {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new SolvedException(CommentErrorCode.COMMENT_NOT_FOUND, "Comment not found"));
        User user = userService.findByUserId(userId)
                .orElseThrow(() -> new SolvedException(CommentErrorCode.COMMENT_NOT_FOUND, "User not found"));

        checkAuthorizationOrThrow(user, comment);
        userService.updateLastActived(user);
        commentRepository.deleteById(commentId);
    }

    private void checkAuthorizationOrThrow(User user, Comment comment) {
        if (!user.isMemberAbove() && !comment.getCreator().equals(user)) {
            throw new AccessDeniedException("수정/삭제 권한이 없습니다.");
        }
    }
}
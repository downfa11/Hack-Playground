package com.ns.solve.utils.mapper;

import com.ns.solve.domain.dto.board.BoardDto;
import com.ns.solve.domain.dto.comment.CommentDto;
import com.ns.solve.domain.dto.problem.ProblemDto;
import com.ns.solve.domain.dto.user.UserDto;
import com.ns.solve.domain.entity.Comment;

public class CommentMapper {
    public static CommentDto mapperToCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();

        dto.setId(comment.getId());
        dto.setContents(comment.getContent());
        dto.setType(comment.getType());

        if (comment.getCreator() != null) {
            UserDto userDto = UserMapper.mapperToUserDto(comment.getCreator());
            dto.setCreator(userDto);
        }

        if (comment.getProblem() != null) {
            ProblemDto problemDto = ProblemMapper.mapperToProblemDto(comment.getProblem());
            dto.setProblem(problemDto);
        }

        if (comment.getBoard() != null) {
            BoardDto boardDto = BoardMapper.mapperToBoardDto(comment.getBoard());
            dto.setBoard(boardDto);
        }

        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());

        return dto;
    }
}

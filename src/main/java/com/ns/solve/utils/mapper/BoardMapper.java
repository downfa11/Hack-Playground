package com.ns.solve.utils.mapper;

import com.ns.solve.domain.dto.board.BoardDto;
import com.ns.solve.domain.entity.Board;

public class BoardMapper {

    public static BoardDto mapperToBoardDto(Board board) {
        return BoardDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .type(board.getType())
                .contents(board.getContents())
                .creator(UserMapper.mapperToUserDto(board.getCreator()))
                .commentList(board.getCommentList())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }
}

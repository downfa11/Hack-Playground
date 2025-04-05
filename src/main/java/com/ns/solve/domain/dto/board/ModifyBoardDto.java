package com.ns.solve.domain.dto.board;

public record ModifyBoardDto(Long boardId, Long userId, String title, String type, String contents){
}

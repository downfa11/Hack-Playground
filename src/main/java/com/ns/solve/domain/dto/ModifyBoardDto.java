package com.ns.solve.domain.dto;

public record ModifyBoardDto(Long boardId, Long userId, String title, String type, String detail){
}

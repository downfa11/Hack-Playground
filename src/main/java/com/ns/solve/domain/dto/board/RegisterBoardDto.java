package com.ns.solve.domain.dto.board;

public record RegisterBoardDto(Long userId, String title, String type, String creator, String contents){
}

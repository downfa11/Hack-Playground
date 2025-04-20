package com.ns.solve.domain.dto.board;

import com.ns.solve.domain.entity.BoardType;

public record RegisterBoardDto(String title, BoardType type, String contents){
}

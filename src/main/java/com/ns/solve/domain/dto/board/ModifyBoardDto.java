package com.ns.solve.domain.dto.board;

import com.ns.solve.domain.vo.BoardType;

public record ModifyBoardDto(String title, BoardType type, String contents){
}

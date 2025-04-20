package com.ns.solve.repository.board;

import com.ns.solve.domain.dto.board.BoardSummary;
import com.ns.solve.domain.entity.BoardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardCustomRepository {
    Page<BoardSummary> findBoardsByPage(BoardType type, Pageable pageable, boolean desc);
}

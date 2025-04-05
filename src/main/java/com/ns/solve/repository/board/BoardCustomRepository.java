package com.ns.solve.repository.board;

import com.ns.solve.domain.dto.board.BoardSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardCustomRepository {
    Page<BoardSummary> findBoardsByPage(Pageable pageable, boolean desc);
}

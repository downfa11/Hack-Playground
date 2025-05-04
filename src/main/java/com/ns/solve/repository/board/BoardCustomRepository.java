package com.ns.solve.repository.board;

import com.ns.solve.domain.dto.board.BoardSummary;
import com.ns.solve.domain.entity.BoardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BoardCustomRepository {
    Page<BoardSummary> searchKeywordInTitle(BoardType type, String keyword, Pageable pageable);
    Page<BoardSummary> findBoardsByPage(BoardType type, Pageable pageable, boolean desc);
}

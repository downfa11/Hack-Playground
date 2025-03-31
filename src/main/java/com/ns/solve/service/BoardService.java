package com.ns.solve.service;

import com.ns.solve.domain.Board;
import com.ns.solve.domain.User;
import com.ns.solve.domain.dto.BoardSummary;
import com.ns.solve.domain.dto.ModifyBoardDto;
import com.ns.solve.domain.dto.RegisterBoardDto;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.repository.board.BoardRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    // Board 반환시 Creator나 CommentList도 같이 뱉으니 N+1 등의 불필요한 쿼리 생성을 경계하자.
    // getAllBoards()는 테스트용이라 신경쓰지말자.

    @Transactional
    public Board createBoard(RegisterBoardDto registerBoardDto) {
        User creator = userRepository.findById(registerBoardDto.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Board board = new Board();
        board.setTitle(registerBoardDto.title());
        board.setType(registerBoardDto.type());
        board.setCreator(creator);

        return boardRepository.save(board);
    }

    @Transactional
    public Board updateBoard(ModifyBoardDto modifyBoardDto) {
        Board board = boardRepository.findById(modifyBoardDto.boardId())
                .orElseThrow(() -> new RuntimeException("Board not found"));

        board.setTitle(modifyBoardDto.title());
        board.setType(modifyBoardDto.type());

        User creator = userRepository.findById(modifyBoardDto.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        board.setCreator(creator);

        return boardRepository.save(board);
    }

    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    public Optional<Board> getBoardById(Long id) {
        return boardRepository.findById(id);
    }

    public void deleteBoard(Long id) {
        boardRepository.deleteById(id);
    }

    public Page<BoardSummary> getBoards(Pageable pageable, boolean desc) {
        return boardRepository.findBoardsByPage(pageable, desc);
    }
}

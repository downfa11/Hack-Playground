package com.ns.solve.service;

import com.ns.solve.domain.dto.board.BoardDto;
import com.ns.solve.domain.entity.Board;
import com.ns.solve.domain.entity.BoardType;
import com.ns.solve.domain.entity.Role;
import com.ns.solve.domain.entity.User;
import com.ns.solve.domain.dto.board.BoardSummary;
import com.ns.solve.domain.dto.board.ModifyBoardDto;
import com.ns.solve.domain.dto.board.RegisterBoardDto;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.repository.board.BoardRepository;
import java.util.List;
import java.util.Optional;

import com.ns.solve.utils.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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
    public BoardDto createBoard(Long userId, RegisterBoardDto registerBoardDto) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (BoardType.ANNOUNCE.equals(registerBoardDto.type()) && !Role.ROLE_ADMIN.equals(creator.getRole())) {
            throw new AccessDeniedException("공지사항은 관리자만 작성할 수 있습니다.");
        }

        Board board = new Board();
        board.setTitle(registerBoardDto.title());
        board.setType(registerBoardDto.type());
        board.setContents(registerBoardDto.contents());
        board.setCreator(creator);

        return BoardMapper.mapperToBoardDto(boardRepository.save(board));
    }

    @Transactional
    public BoardDto updateBoard(Long userId, Long boardId, ModifyBoardDto modifyBoardDto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (BoardType.ANNOUNCE.equals(modifyBoardDto.type()) && !Role.ROLE_ADMIN.equals(user.getRole())) {
            throw new AccessDeniedException("공지사항은 관리자만 작성할 수 있습니다.");
        }

        checkAuthorizationOrThrow(user, board);

        board.setTitle(modifyBoardDto.title());
        board.setType(modifyBoardDto.type());
        board.setContents(modifyBoardDto.contents());

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        board.setCreator(creator);

        return BoardMapper.mapperToBoardDto(boardRepository.save(board));
    }

    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    public Optional<BoardDto> getBoardById(Long id) {
        return boardRepository.findById(id)
                .map(BoardMapper::mapperToBoardDto);
    }
    public void deleteBoard(Long userId, Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        checkAuthorizationOrThrow(user, board);
        boardRepository.deleteById(boardId);
    }

    public Page<BoardSummary> getBoards(BoardType type, Pageable pageable, boolean desc) {
        return boardRepository.findBoardsByPage(type, pageable, desc);
    }

    private void checkAuthorizationOrThrow(User user, Board board) {
        if (!user.isMemberAbove() && !board.getCreator().equals(user)) {
            throw new AccessDeniedException("수정/삭제 권한이 없습니다.");
        }
    }
}

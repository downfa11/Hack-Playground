package com.ns.solve.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ns.solve.domain.dto.board.BoardDto;
import com.ns.solve.domain.entity.Board;
import com.ns.solve.domain.entity.BoardType;
import com.ns.solve.domain.entity.User;
import com.ns.solve.domain.dto.board.BoardSummary;
import com.ns.solve.domain.dto.board.RegisterBoardDto;
import com.ns.solve.domain.dto.board.ModifyBoardDto;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.repository.board.BoardRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock private BoardRepository boardRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private BoardService boardService;

    private Board testBoard;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setNickname("testUser");

        testBoard = new Board();
        testBoard.setId(1L);
        testBoard.setTitle("title");
        testBoard.setType(BoardType.FREE);
        testBoard.setCreator(testUser);
    }

    @Test
    void testCreateBoard() {
        Long userId = 1L;
        RegisterBoardDto dto = new RegisterBoardDto("title", BoardType.FREE, "details");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(boardRepository.save(any(Board.class))).thenReturn(testBoard);

        BoardDto createdBoard = boardService.createBoard(userId, dto);

        assertNotNull(createdBoard);
        assertEquals(dto.title(), createdBoard.getTitle());
        assertEquals(dto.type(), createdBoard.getType());
        assertEquals(testUser, createdBoard.getCreator());
        verify(boardRepository, times(1)).save(any(Board.class));
    }

    @Test
    void testGetAllBoards() {
        when(boardRepository.findAll()).thenReturn(List.of(testBoard));

        List<Board> boards = boardService.getAllBoards();

        assertFalse(boards.isEmpty());
        assertEquals(1, boards.size());
        assertEquals(testBoard.getTitle(), boards.get(0).getTitle());
        verify(boardRepository, times(1)).findAll();
    }

    @Test
    void testGetBoardById() {
        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoard));

        Optional<BoardDto> board = boardService.getBoardById(1L);

        assertTrue(board.isPresent());
        assertEquals(testBoard.getTitle(), board.get().getTitle());
        verify(boardRepository, times(1)).findById(1L);
    }

    @Test
    void testUpdateBoard() {
        Long userId = 1L, boardId = 1L;
        ModifyBoardDto dto = new ModifyBoardDto( "new title", BoardType.ANNOUNCE, "updated details");

        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoard));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(boardRepository.save(any(Board.class))).thenReturn(testBoard);

        BoardDto updatedBoard = boardService.updateBoard(userId, boardId, dto);

        assertNotNull(updatedBoard);
        assertEquals(dto.title(), updatedBoard.getTitle());
        assertEquals(dto.type(), updatedBoard.getType());
        assertEquals(testUser, updatedBoard.getCreator());
        verify(boardRepository, times(1)).findById(boardId);
        verify(boardRepository, times(1)).save(any(Board.class));
    }

    @Test
    void testDeleteBoard() {
        doNothing().when(boardRepository).deleteById(1L);

        boardService.deleteBoard(1L, 1L);

        verify(boardRepository, times(1)).deleteById(1L);
    }

    @Test
    void testGetBoards() {
        BoardType boardType = BoardType.FREE;
        Pageable pageable = PageRequest.of(0, 10);
        Page<BoardSummary> page = new PageImpl<>(List.of());

        when(boardRepository.findBoardsByPage(boardType, pageable, true)).thenReturn(page);

        Page<BoardSummary> result = boardService.getBoards(boardType, pageable, true);

        assertNotNull(result);
        verify(boardRepository, times(1)).findBoardsByPage(boardType, pageable, true);
    }
}

package com.ns.solve.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ns.solve.domain.Board;
import com.ns.solve.domain.User;
import com.ns.solve.domain.dto.BoardSummary;
import com.ns.solve.domain.dto.RegisterBoardDto;
import com.ns.solve.domain.dto.ModifyBoardDto;
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
        testBoard.setType("free");
        testBoard.setCreator(testUser);
    }

    @Test
    void testCreateBoard() {
        RegisterBoardDto dto = new RegisterBoardDto(1L, "title", "free", "testUser", "details");

        when(userRepository.findById(dto.userId())).thenReturn(Optional.of(testUser));
        when(boardRepository.save(any(Board.class))).thenReturn(testBoard);

        Board createdBoard = boardService.createBoard(dto);

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

        Optional<Board> board = boardService.getBoardById(1L);

        assertTrue(board.isPresent());
        assertEquals(testBoard.getTitle(), board.get().getTitle());
        verify(boardRepository, times(1)).findById(1L);
    }

    @Test
    void testUpdateBoard() {
        ModifyBoardDto dto = new ModifyBoardDto(1L,1L, "new title", "notice", "updated details");

        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoard));
        when(userRepository.findById(dto.userId())).thenReturn(Optional.of(testUser));
        when(boardRepository.save(any(Board.class))).thenReturn(testBoard);

        Board updatedBoard = boardService.updateBoard(dto);

        assertNotNull(updatedBoard);
        assertEquals(dto.title(), updatedBoard.getTitle());
        assertEquals(dto.type(), updatedBoard.getType());
        assertEquals(testUser, updatedBoard.getCreator());
        verify(boardRepository, times(1)).findById(1L);
        verify(boardRepository, times(1)).save(any(Board.class));
    }

    @Test
    void testDeleteBoard() {
        doNothing().when(boardRepository).deleteById(1L);

        boardService.deleteBoard(1L);

        verify(boardRepository, times(1)).deleteById(1L);
    }

    @Test
    void testGetBoards() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BoardSummary> page = new PageImpl<>(List.of());

        when(boardRepository.findBoardsByPage(pageable, true)).thenReturn(page);

        Page<BoardSummary> result = boardService.getBoards(pageable, true);

        assertNotNull(result);
        verify(boardRepository, times(1)).findBoardsByPage(pageable, true);
    }
}

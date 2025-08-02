package com.ns.solve.controller;

import com.ns.solve.domain.dto.board.BoardDto;
import com.ns.solve.domain.entity.Board;
import com.ns.solve.domain.dto.board.BoardSummary;
import com.ns.solve.domain.dto.MessageEntity;
import com.ns.solve.domain.dto.board.ModifyBoardDto;
import com.ns.solve.domain.dto.board.RegisterBoardDto;
import com.ns.solve.domain.vo.BoardType;
import com.ns.solve.service.BoardService;
import com.ns.solve.utils.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardController {
    private final BoardService boardService;

    @Operation(summary = "게시판 생성", description = "새로운 게시판을 생성합니다. BoardType(Enum)=ANNOUNCE, FREE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "게시판이 성공적으로 생성되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    @PostMapping
    public ResponseEntity<MessageEntity> createBoard(@RequestBody RegisterBoardDto registerBoardDto, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        BoardDto createdBoard = boardService.createBoard(userId, registerBoardDto);
        return new ResponseEntity<>(new MessageEntity("Board created successfully", createdBoard), HttpStatus.CREATED);
    }

    @Operation(summary = "모든 게시판 조회", description = "모든 게시판을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시판들이 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "게시판을 찾을 수 없습니다.")
    })
    @GetMapping("/all")
    public ResponseEntity<MessageEntity> getAllBoards() {
        List<Board> boards = boardService.getAllBoards();
        return new ResponseEntity<>(new MessageEntity("Boards fetched successfully", boards), HttpStatus.OK);
    }

    @Operation(summary = "게시판 정렬된 상태로 조회", description = "게시글을 정렬된 상태로 조회합니다. BoardType(Enum)=ANNOUNCE, FREE, sortByNewest가 true면 최신글순")
    @GetMapping
    public ResponseEntity<MessageEntity> getBoards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "free") BoardType type,
            @RequestParam(defaultValue = "true") boolean sortByNewest) {

        Page<BoardSummary> boardSummaries = boardService.getBoards(type, PageRequest.of(page, size), sortByNewest);
        return new ResponseEntity<>(new MessageEntity("게시글 목록 조회 성공", boardSummaries), HttpStatus.OK);
    }


    @Operation(summary = "게시판 ID로 조회", description = "주어진 게시판 ID로 게시판을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시판이 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "게시판을 찾을 수 없습니다.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MessageEntity> getBoardById(@PathVariable Long id) {
        Optional<BoardDto> board = boardService.getBoardById(id);
        if (board.isPresent()) {
            return new ResponseEntity<>(new MessageEntity("Board found", board.get()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new MessageEntity("Board not found", null), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "게시판 검색", description = "BoardType과 키워드로 게시판을 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시판 검색 결과가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "검색된 게시판이 없습니다.")})
    @GetMapping("/search")
    public ResponseEntity<MessageEntity> searchBoard(@RequestParam BoardType type, @RequestParam String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        Page<BoardSummary> results = boardService.searchBoard(type, keyword, PageRequest.of(page, size));

        if (results.hasContent()) {
            return ResponseEntity.ok(new MessageEntity("게시판 검색 성공", results));
        } else {
            return new ResponseEntity<>(new MessageEntity("검색된 게시판이 없습니다.", null), HttpStatus.NOT_FOUND);
        }
    }


    @Operation(summary = "게시판 수정", description = "주어진 게시판 ID로 게시판을 수정합니다. BoardType(Enum)=ANNOUNCE, FREE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시판이 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "404", description = "게시판을 찾을 수 없습니다.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MessageEntity> updateBoard(@PathVariable Long id, @RequestBody ModifyBoardDto modifyBoardDto, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        BoardDto updated = boardService.updateBoard(userId, id, modifyBoardDto);
        return ResponseEntity.ok(new MessageEntity("Board updated successfully", updated));
    }

    @Operation(summary = "게시판 삭제", description = "주어진 게시판 ID로 게시글을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글이 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageEntity> deleteBoard(@PathVariable Long id, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        boardService.deleteBoard(userId, id);
        return ResponseEntity.ok(new MessageEntity("Board deleted successfully", null));
    }
}

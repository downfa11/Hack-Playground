package com.ns.solve.domain.dto.board;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ns.solve.domain.dto.user.UserDto;
import com.ns.solve.domain.entity.BoardType;
import com.ns.solve.domain.entity.Comment;
import com.ns.solve.domain.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BoardDto {
    private Long id;
    private String title;

    private BoardType type;  // 공지사항, 자유게시판
    private String contents;
    private UserDto creator;

    private List<Comment> commentList;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

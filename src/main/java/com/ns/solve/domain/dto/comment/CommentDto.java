package com.ns.solve.domain.dto.comment;

import com.ns.solve.domain.dto.board.BoardDto;
import com.ns.solve.domain.dto.problem.ProblemDto;
import com.ns.solve.domain.dto.user.UserDto;
import com.ns.solve.domain.vo.CommentType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CommentDto {
    private Long id;
    private String contents;

    private UserDto creator;

    private CommentType type;
    private ProblemDto problem;
    private BoardDto board;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

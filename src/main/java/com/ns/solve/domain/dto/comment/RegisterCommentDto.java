package com.ns.solve.domain.dto.comment;

import com.ns.solve.domain.vo.CommentType;

public record RegisterCommentDto(CommentType type, Long parentId, String contents){
}

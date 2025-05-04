package com.ns.solve.domain.dto.comment;

import com.ns.solve.domain.entity.CommentType;

public record ModifyCommentDto(CommentType type, String contents){
}

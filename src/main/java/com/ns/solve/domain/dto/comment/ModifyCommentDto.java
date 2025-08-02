package com.ns.solve.domain.dto.comment;

import com.ns.solve.domain.vo.CommentType;

public record ModifyCommentDto(CommentType type, String contents){
}

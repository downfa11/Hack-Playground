package com.ns.solve.domain.dto.comment;

public record RegisterCommentDto(String type, Long parentId, Long userId, String contents){
}

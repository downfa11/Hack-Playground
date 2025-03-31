package com.ns.solve.domain.dto;

public record RegisterCommentDto(String type, Long parentId, Long userId, String content){
}

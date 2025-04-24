package com.ns.solve.utils.exception.ErrorCode;

import org.springframework.http.HttpStatus;

public enum CommentErrorCode implements BaseErrorCode {
    COMMENT_NOT_FOUND("Comment not found", HttpStatus.NOT_FOUND),
    INVALID_COMMENT_OPERATION("Invalid comment operation", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED("Permission denied to modify/delete comment", HttpStatus.FORBIDDEN);

    private final String message;
    private final HttpStatus status;

    CommentErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }
}

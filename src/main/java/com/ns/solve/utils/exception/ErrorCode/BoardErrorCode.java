package com.ns.solve.utils.exception.ErrorCode;

import org.springframework.http.HttpStatus;

public enum BoardErrorCode implements BaseErrorCode {
    BOARD_NOT_FOUND("Board not found", HttpStatus.NOT_FOUND),
    INVALID_BOARD_OPERATION("Invalid board operation", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED("Permission denied to modify/delete board", HttpStatus.FORBIDDEN);

    private final String message;
    private final HttpStatus status;

    BoardErrorCode(String message, HttpStatus status) {
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


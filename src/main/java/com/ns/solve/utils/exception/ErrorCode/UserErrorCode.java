package com.ns.solve.utils.exception.ErrorCode;

import com.ns.solve.utils.exception.ErrorCode.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum UserErrorCode implements BaseErrorCode {
    USER_NOT_FOUND("User not found", HttpStatus.NOT_FOUND),
    INVALID_NICKNAME_OR_ACCOUNT("Invalid nickname or account", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED("자기 자신만 수정할 수 있습니다.", HttpStatus.FORBIDDEN);

    private final String message;
    private final HttpStatus status;

    UserErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}


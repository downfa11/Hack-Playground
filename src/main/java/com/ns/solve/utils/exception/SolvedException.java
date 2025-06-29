package com.ns.solve.utils.exception;

import com.ns.solve.utils.exception.ErrorCode.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SolvedException extends RuntimeException {
    private final BaseErrorCode errorCode;
    private final String message;

    public SolvedException(BaseErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }

    public SolvedException(BaseErrorCode errorCode, String detail) {
        this.errorCode = errorCode;
        this.message = errorCode.getMessage() + " - " + detail;
    }

    @Override
    public String getMessage() {
        return "[%s] %s".formatted(errorCode.name(), message);
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getStatus();
    }
}

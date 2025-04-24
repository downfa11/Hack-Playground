package com.ns.solve.utils.exception.ErrorCode;

import org.springframework.http.HttpStatus;

public enum ProblemErrorCode implements BaseErrorCode {
    PROBLEM_NOT_FOUND("Problem not found", HttpStatus.NOT_FOUND),
    INVALID_PROBLEM_OPERATION("Invalid problem operation", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED("Permission denied to modify/delete problem", HttpStatus.FORBIDDEN),
    FILE_NOT_FOUND("File not found for problem", HttpStatus.NOT_FOUND),
    FILE_UPLOAD_FAILED("File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_PROBLEM_TYPE("Invalid problem type", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus status;

    ProblemErrorCode(String message, HttpStatus status) {
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

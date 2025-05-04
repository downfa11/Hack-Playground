package com.ns.solve.utils.exception;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class ErrorResponse {
    private final String code;
    private final String message;
    private final LocalDateTime timestamp;

    public static ErrorResponse from(SolvedException e) {
        return ErrorResponse.builder()
                .code(e.getErrorCode().name())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}

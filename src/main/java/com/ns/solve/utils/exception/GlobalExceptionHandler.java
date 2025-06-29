package com.ns.solve.utils.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SolvedException.class)
    public ResponseEntity<Map<String, Object>> handleSolvedException(SolvedException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getErrorCode().name());
        body.put("message", ex.getMessage());

        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }
}

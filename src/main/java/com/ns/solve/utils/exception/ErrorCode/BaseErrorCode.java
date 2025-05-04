package com.ns.solve.utils.exception.ErrorCode;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
    String getMessage();
    HttpStatus getStatus();
    String name();
}

package com.ns.solve.utils.exception.ErrorCode;

import org.springframework.http.HttpStatus;

public enum FileErrorCode implements BaseErrorCode {
    FILE_NOT_FOUND("File not found", HttpStatus.NOT_FOUND),
    FILE_UPLOAD_ERROR("File upload error", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_ERROR("File delete error", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_READ_ERROR("File read error", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DOWNLOAD_ERROR("File download error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_FILE_TYPE("Invalid file type", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus status;

    FileErrorCode(String message, HttpStatus status) {
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

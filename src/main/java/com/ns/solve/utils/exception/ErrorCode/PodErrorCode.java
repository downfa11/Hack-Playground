package com.ns.solve.utils.exception.ErrorCode;

import org.springframework.http.HttpStatus;

public enum PodErrorCode implements BaseErrorCode {
    SERVICE_NOT_FOUND("Filtering Label does not exist.", HttpStatus.NOT_FOUND),
    K8S_API_ERROR("Error occurred with the Kubernetes API.", HttpStatus.INTERNAL_SERVER_ERROR),
    POD_NOT_FOUND("Pod not found in namespace.", HttpStatus.NOT_FOUND),
    POD_DELETE_ERROR("Failed to delete the pod.", HttpStatus.INTERNAL_SERVER_ERROR),
    LABEL_SELECTOR_ERROR("Error in label selector.", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus status;

    PodErrorCode(String message, HttpStatus status) {
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

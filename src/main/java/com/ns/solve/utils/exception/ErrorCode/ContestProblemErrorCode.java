package com.ns.solve.utils.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ContestProblemErrorCode implements BaseErrorCode {
    CONTEST_PROBLEM_NOT_FOUND(HttpStatus.NOT_FOUND, "대회 문제를 찾을 수 없습니다."),
    DUPLICATE_PROBLEM_TITLE(HttpStatus.CONFLICT, "해당 대회에 이미 같은 이름의 문제가 존재합니다.");

    private final HttpStatus status;
    private final String message;
}
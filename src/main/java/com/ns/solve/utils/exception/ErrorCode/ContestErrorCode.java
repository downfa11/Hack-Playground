package com.ns.solve.utils.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ContestErrorCode implements BaseErrorCode {
    CONTEST_NOT_FOUND(HttpStatus.NOT_FOUND, "대회를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    ALREADY_REGISTERED(HttpStatus.CONFLICT, "사용자는 이미 대회에 등록되어 있습니다."),
    CONTEST_NOT_ENDED(HttpStatus.INTERNAL_SERVER_ERROR, "대회가 종료되지 않았습니다."),
    CONTEST_NOT_UPCOMING(HttpStatus.INTERNAL_SERVER_ERROR, "대회가 시작되지 않았습니다."),
    NOT_ELIGIBLE_AFFILIATION(HttpStatus.UNAUTHORIZED, "대회의 참가 조건을 만족하지 않습니다.");
    private final HttpStatus status;
    private final String message;
}
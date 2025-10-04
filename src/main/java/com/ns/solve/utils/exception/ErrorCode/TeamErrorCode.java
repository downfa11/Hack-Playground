package com.ns.solve.utils.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TeamErrorCode implements BaseErrorCode {
    CONTEST_NOT_FOUND(HttpStatus.NOT_FOUND, "대회를 찾을 수 없습니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DUPLICATE_TEAM_NAME(HttpStatus.CONFLICT, "이미 존재하는 팀 이름입니다."),
    ALREADY_JOINED_TEAM(HttpStatus.CONFLICT, "사용자는 이미 다른 팀에 속해 있습니다."),
    INVALID_CONTEST_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "잘못된 대회 유형입니다."),
    AFFILIATION_NOT_SELECTED(HttpStatus.NOT_FOUND, "해당 소속을 찾을 수 없습니다."),
    TEAM_FULL(HttpStatus.INTERNAL_SERVER_ERROR, "해당 팀의 정원을 초과했습니다.");

    private final HttpStatus status;
    private final String message;
}
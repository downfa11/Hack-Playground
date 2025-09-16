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
    ALREADY_JOINED_TEAM(HttpStatus.CONFLICT, "사용자는 이미 다른 팀에 속해 있습니다.");

    private final HttpStatus status;
    private final String message;
}
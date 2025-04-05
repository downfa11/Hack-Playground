package com.ns.solve.domain.dto.user;

import java.time.LocalDateTime;

public record UserRankDto(String nickname, Long solvedCount, LocalDateTime registered, LocalDateTime lastActived) {
}

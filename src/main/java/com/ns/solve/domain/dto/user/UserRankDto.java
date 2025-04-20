package com.ns.solve.domain.dto.user;

import java.time.LocalDateTime;

public record UserRankDto(Long rank, String nickname, Long solvedCount, LocalDateTime registered, LocalDateTime lastActived) {
}

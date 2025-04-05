package com.ns.solve.domain.dto.user;

import java.time.LocalDateTime;

public record UserDto(String nickname, Long score, LocalDateTime lastActived) {
}

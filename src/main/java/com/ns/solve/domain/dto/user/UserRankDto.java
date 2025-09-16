package com.ns.solve.domain.dto.user;

import com.ns.solve.domain.entity.user.Affiliation;

import java.time.LocalDateTime;
import java.util.List;

public record UserRankDto(Long rank, String nickname, Long solvedCount, List<Affiliation> affiliations,LocalDateTime registered, LocalDateTime lastActived) {
}

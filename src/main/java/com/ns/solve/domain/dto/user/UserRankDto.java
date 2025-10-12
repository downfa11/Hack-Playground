package com.ns.solve.domain.dto.user;

import java.time.LocalDateTime;
import java.util.List;

public record UserRankDto(Long rank, String nickname, Long solvedCount, List<AffiliationDto> affiliations,LocalDateTime registered, LocalDateTime lastActived) {
}

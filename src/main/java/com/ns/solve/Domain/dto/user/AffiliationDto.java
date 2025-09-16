package com.ns.solve.domain.dto.user;

import com.ns.solve.domain.entity.user.Affiliation;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AffiliationDto {
    private Long id;
    private String name;

    public static AffiliationDto from(Affiliation affiliation) {
        return AffiliationDto.builder()
                .id(affiliation.getId())
                .name(affiliation.getName())
                .build();
    }
}
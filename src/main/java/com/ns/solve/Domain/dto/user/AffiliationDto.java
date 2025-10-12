package com.ns.solve.domain.dto.user;

import com.ns.solve.domain.entity.user.Affiliation;
import com.ns.solve.domain.vo.AffiliationType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AffiliationDto {
    private Long id;
    private String name;
    private AffiliationType type;

    public static AffiliationDto from(Affiliation affiliation) {
        if (affiliation == null) return null;
        return AffiliationDto.builder()
                .id(affiliation.getId())
                .name(affiliation.getName())
                .type(affiliation.getType())
                .build();
    }
}
package com.ns.solve.domain.dto.contest;

import com.ns.solve.domain.dto.user.AffiliationDto;
import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.user.Affiliation;
import com.ns.solve.domain.vo.AffiliationType;
import com.ns.solve.domain.vo.ContestStatus;
import com.ns.solve.domain.vo.ContestType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
public class ContestDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ContestType type;
    private ContestStatus status;
    private String organizerName;
    private Integer maxTeamSize;
    private String prize;
    private String rules;
    private Set<AffiliationType> affiliationTypes;
    private List<AffiliationDto> affiliations;

    public static ContestDto from(Contest contest) {
        return ContestDto.builder()
                .id(contest.getId())
                .title(contest.getTitle())
                .description(contest.getDescription())
                .startTime(contest.getStartTime())
                .endTime(contest.getEndTime())
                .type(contest.getType())
                .status(contest.getStatus())
                .organizerName(contest.getOrganizerName())
                .maxTeamSize(contest.getMaxTeamSize())
                .prize(contest.getPrize())
                .rules(contest.getRules())
                .affiliationTypes(contest.getAffiliationTypes())
                .affiliations(contest.getAffiliations().stream()
                        .map(AffiliationDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
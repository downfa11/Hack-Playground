package com.ns.solve.domain.dto.contest;

import com.ns.solve.domain.entity.contest.Team;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamDto {
    private Long id;
    private String name;
    private String description;
    private Long contestId;

    public static TeamDto from(Team team) {
        return TeamDto.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .contestId(team.getContest().getId())
                .build();
    }
}
package com.ns.solve.domain.dto.contest;

import com.ns.solve.domain.entity.contest.Team;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamDto {
    private Long teamId;
    private Long contestId;
    private String name;
    private String password;
    private int score;

    public static TeamDto from(Team team) {
        return TeamDto.builder()
                .teamId(team.getId())
                .name(team.getName())
                .password(team.getPassword())
                .contestId(team.getContest().getId())
                .score(team.getPoints())
                .build();
    }
}
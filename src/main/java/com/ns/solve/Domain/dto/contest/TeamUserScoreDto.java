package com.ns.solve.domain.dto.contest;

import com.ns.solve.domain.entity.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamUserScoreDto {
    private String name;
    private int score;

    public static TeamUserScoreDto from(User user, int points) {
        return TeamUserScoreDto.builder()
                .name(user.getNickname())
                .score(points)
                .build();
    }
}
package com.ns.solve.domain.dto.contest;

import com.ns.solve.domain.entity.contest.Contest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserContestDto {
    private Long contestId;
    private String contestName;
    private boolean winner;
    private int rank;       // 수상 순위 (없으면 0)

    public static UserContestDto from(Contest contest, boolean winner, int rank) {
        return UserContestDto.builder()
                .contestId(contest.getId())
                .contestName(contest.getTitle())
                .winner(winner)
                .rank(rank)
                .build();
    }
}

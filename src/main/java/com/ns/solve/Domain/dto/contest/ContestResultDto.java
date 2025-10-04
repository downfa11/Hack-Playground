package com.ns.solve.domain.dto.contest;

import com.ns.solve.domain.dto.user.UserDto;
import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.contest.Prize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
public class ContestResultDto {
    private Long id;
    private String title;
    private List<PrizeResultDto> prizes;

    public static ContestResultDto from(Contest contest) {
        return ContestResultDto.builder()
                .id(contest.getId())
                .title(contest.getTitle())
                .prizes(contest.getPrizes().stream()
                        .map(PrizeResultDto::from)
                        .collect(Collectors.toList()))
                .build();
    }

    @Getter
    @Setter
    @Builder
    public static class PrizeResultDto {
        private String name;
        private int rank;
        private int numberOfWinners;
        private List<String> winners;

        public static PrizeResultDto from(Prize prize) {
            return PrizeResultDto.builder()
                    .name(prize.getName())
                    .rank(prize.getRank())
                    .numberOfWinners(prize.getNumberOfWinners())
                    .winners(prize.getWinners().stream()
                            .map(user -> user.getNickname())
                            .collect(Collectors.toList()))
                    .build();
        }
    }
}
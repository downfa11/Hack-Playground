package com.ns.solve.domain.dto.contest;

import com.ns.solve.domain.vo.ContestType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class RegisterContestRequest {
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ContestType type;
    private Integer maxTeamSize;
    private String organizerName;
    private List<Long> organizerIds;
    private List<Long> affiliationIds;
    private boolean isPrizeEnabled;
    private String prizeMoney;
    private List<PrizeDto> prizes;
    private String rules;
    private boolean reviewConsent;

    @Getter
    @Setter
    @Builder
    public static class PrizeDto {
        private String name;
        private int numberOfWinners;
    }
}
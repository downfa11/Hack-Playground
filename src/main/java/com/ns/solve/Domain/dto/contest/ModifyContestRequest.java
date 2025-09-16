package com.ns.solve.domain.dto.contest;

import com.ns.solve.domain.vo.AffiliationType;
import com.ns.solve.domain.vo.ContestType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
public class ModifyContestRequest {
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ContestType type;
    private Integer maxTeamSize;
    private String organizerName;
    private List<Long> organizerIds;
    private Set<AffiliationType> affiliationTypes;
    private List<Long> affiliationIds;
    private boolean isPrizeEnabled;
    private String prizeMoney;
    private List<PrizeDto> prizes;
    private String rules;

    @Getter
    @Setter
    @Builder
    public static class PrizeDto {
        private String name;
        private int numberOfWinners;
    }
}
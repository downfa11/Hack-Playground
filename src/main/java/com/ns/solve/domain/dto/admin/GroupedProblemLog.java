package com.ns.solve.domain.dto.admin;

import com.ns.solve.domain.vo.OperationType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class GroupedProblemLog {
    private String problemTitle;
    private String creatorUsername;
    private LocalDateTime latestCreatedAt;
    private List<OperationType> operationTypes = new ArrayList<>();

    public GroupedProblemLog(String problemTitle, String creatorUsername) {
        this.problemTitle = problemTitle;
        this.creatorUsername = creatorUsername;
    }
}

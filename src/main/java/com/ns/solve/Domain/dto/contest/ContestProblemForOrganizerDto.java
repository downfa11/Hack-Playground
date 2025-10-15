package com.ns.solve.domain.dto.contest;

import com.ns.solve.domain.dto.user.UserDto;
import com.ns.solve.domain.vo.ContestProblemDifficulty;
import com.ns.solve.domain.vo.WargameKind;
import io.kubernetes.client.proto.V1Networking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContestProblemForOrganizerDto {
    private Long id;
    private String title;
    private String detail;
    private UserDto creator;
    private WargameKind kind;
    private ContestProblemDifficulty difficulty;
    private List<String> tags;
    private String flag;
    private Integer points;
    private String dockerfileLink;
    private Integer portNumber;
    private String problemFile;
    private boolean hasContainer;
    private boolean isLocked;

    private Double entireCount;
    private Double correctCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

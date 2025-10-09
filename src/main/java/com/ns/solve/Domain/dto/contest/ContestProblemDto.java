package com.ns.solve.domain.dto.contest;

import com.ns.solve.domain.dto.user.UserDto;
import com.ns.solve.domain.vo.WargameKind;
import com.ns.solve.domain.vo.ContestProblemDifficulty;
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
public class ContestProblemDto {
    private Long id;
    private String title;
    private String detail;
    private UserDto creator;
    private WargameKind kind;
    private ContestProblemDifficulty difficulty;
    private List<String> tags;
    private Integer points;
    private String flag;
    private String dockerfileLink;
    private String problemFile;
    private boolean hasContainer;
    private boolean isLocked;
    private String source; // contest name
    private boolean isNew;
    private boolean solved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

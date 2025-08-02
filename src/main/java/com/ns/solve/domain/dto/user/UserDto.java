package com.ns.solve.domain.dto.user;

import com.ns.solve.domain.entity.user.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String nickname;
    private Role role;
    private String provider;
    private String account;
    private Long entireScore;
    private Map<String, Long> fieldScores;

    private List<String> solvedProblem;
    private LocalDateTime created;
    private LocalDateTime lastActived;
}

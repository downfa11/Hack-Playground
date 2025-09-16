package com.ns.solve.domain.dto.user;

import com.ns.solve.domain.entity.user.Affiliation;
import com.ns.solve.domain.entity.user.Role;
import com.ns.solve.domain.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

    private List<AffiliationDto> affiliations;

    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .role(user.getRole())
                .provider(user.getProvider())
                .account(user.getAccount())
                .entireScore(user.getScore())
                .fieldScores(user.getFieldScores())
                .created(user.getCreated())
                .lastActived(user.getLastActived())
                .affiliations(user.getAffiliations().stream()
                        .map(AffiliationDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
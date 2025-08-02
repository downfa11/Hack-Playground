package com.ns.solve.domain.dto.user;

import com.ns.solve.domain.entity.user.Role;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserFirstBloodDto {
    private Long id;
    private String nickname;
    private Role role;
    private LocalDateTime firstBlood;

    public UserFirstBloodDto(Long id, String nickname, Role role, LocalDateTime firstBlood) {
        this.id = id;
        this.nickname = nickname;
        this.role = role;
        this.firstBlood = firstBlood;
    }
}

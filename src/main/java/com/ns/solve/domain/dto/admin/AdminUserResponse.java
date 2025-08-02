package com.ns.solve.domain.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String nickname;
    private String account;
    private String role;
    private String created;
    private String lastActived;
    private Long score;
}
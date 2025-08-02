package com.ns.solve.domain.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserUpdateRequest {
    private String nickname;
    private String account;
    private String role;
}

package com.ns.solve.domain.dto.user;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserRequest {
    @NotBlank
    private String account;

    @NotBlank
    private String password;
}

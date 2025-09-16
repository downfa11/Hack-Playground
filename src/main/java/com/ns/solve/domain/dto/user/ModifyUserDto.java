package com.ns.solve.domain.dto.user;

import java.util.List;

public record ModifyUserDto(String nickname, String account, String password, List<Long> affiliationIds) { }

package com.ns.solve.domain.entity.user;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {
    ROLE_MEMBER,     // ordinal = 0
    ROLE_CREATOR,    // ordinal = 1
    ROLE_VALIDATOR,  // ordinal = 2
    ROLE_ADMIN;      // ordinal = 3

    public static Role fromString(String role) {
        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(role)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + role);
    }
}
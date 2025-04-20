package com.ns.solve.domain.dto.user;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JWtToken {

    @Getter
    private final String membershipId;
    @Getter
    private final String nickName;
    @Getter
    private final String jwtToken;
    @Getter
    private final String refreshToken;

    public static JWtToken generateJwtToken(
            String membershipId,
            String nickName,
            String jwtToken,
            String refreshToken) {

        return new JWtToken(membershipId, nickName, jwtToken, refreshToken);
    }

}

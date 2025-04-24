package com.ns.solve.service;


import com.ns.solve.domain.entity.User;
import com.ns.solve.domain.dto.user.JWtToken;
import com.ns.solve.domain.dto.user.LoginUserRequest;
import com.ns.solve.utils.JWTUtil;
import com.ns.solve.utils.exception.SolvedException;
import com.ns.solve.utils.exception.ErrorCode.UserErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class AuthService {

    @Value("${jwt.access.expiration}")
    public Long ACCESS_TOKEN_EXPIRATION;

    @Value("${jwt.refresh.expiration}")
    public Long REFRESH_TOKEN_EXPIRATION;

    private final JWTUtil jwtUtil;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    public JWtToken login(LoginUserRequest request) {
        User user = userService.getUserByAccount(request.getAccount());
        if (user == null) throw new SolvedException(UserErrorCode.USER_NOT_FOUND);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new SolvedException(UserErrorCode.INVALID_NICKNAME_OR_ACCOUNT);
        }

        String jwt = jwtUtil.createJwt(user.getId(), user.getAccount(), user.getRole().name(), ACCESS_TOKEN_EXPIRATION);
        String refreshToken = jwtUtil.createJwt(user.getId(), user.getAccount(), user.getRole().name(), REFRESH_TOKEN_EXPIRATION);

        return JWtToken.generateJwtToken(String.valueOf(user.getId()),user.getAccount(),jwt,refreshToken);
    }

    public boolean validateJwtToken(String token) {
        try {
            return !jwtUtil.isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public User getUserByJwtToken(String token) {
        try {
            if (jwtUtil.isExpired(token)) return null;
            String account = jwtUtil.getAccount(token);
            return userService.getUserByAccount(account);
        } catch (Exception e) {
            return null;
        }
    }

    public JWtToken reissue(String refreshToken) {
        if (!validateJwtToken(refreshToken)) {
            throw new SolvedException(UserErrorCode.ACCESS_DENIED);
        }

        String account = jwtUtil.getAccount(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        User user = userService.getUserByAccount(account);
        if (user == null) throw new SolvedException(UserErrorCode.USER_NOT_FOUND);

        String newAccessToken = jwtUtil.createJwt(user.getId(), user.getAccount(), role, ACCESS_TOKEN_EXPIRATION);
        return JWtToken.generateJwtToken(String.valueOf(user.getId()), user.getNickname(), newAccessToken, refreshToken);

    }

}

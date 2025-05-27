package com.ns.solve.utils;

import com.ns.solve.domain.entity.User;
import com.ns.solve.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    @Value("${jwt.access.expiration}")
    public Long ACCESS_TOKEN_EXPIRATION;

    @Value("${jwt.refresh.expiration}")
    public Long REFRESH_TOKEN_EXPIRATION;

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String oauthId = extractOauthId(oAuth2User.getAttributes());

        if (oauthId == null) {
            log.error("OAuth account null");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "OAuth account null");
            return;
        }

        User user = userRepository.findByAccount(oauthId);
        if (user == null) {
            log.error("user is null");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "등록되지 않은 사용자입니다.");
            return;
        }

        String accessToken = jwtUtil.createJwt(user.getId(), user.getNickname(), user.getRole().name(), ACCESS_TOKEN_EXPIRATION);
        String refreshToken = jwtUtil.createJwt(user.getId(), user.getNickname(), user.getRole().name(), REFRESH_TOKEN_EXPIRATION);

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) (REFRESH_TOKEN_EXPIRATION / 1000));
        response.addCookie(refreshCookie);

        String redirectUrl = UriComponentsBuilder.fromUriString("https://hpground.xyz")
                .queryParam("token", accessToken)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private String extractOauthId(Map<String, Object> attributes) {
        log.warn("OAuth2 Attributes: {}", attributes);

        if (attributes.containsKey("sub")) {
            return (String) attributes.get("sub");
        }

        if (attributes.containsKey("id")) {
            return String.valueOf(attributes.get("id"));
        }

        return null;
    }
}

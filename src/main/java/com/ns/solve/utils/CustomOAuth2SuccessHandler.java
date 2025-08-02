package com.ns.solve.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ns.solve.domain.entity.user.User;
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
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    @Value("${jwt.access.expiration}")
    public Long ACCESS_TOKEN_EXPIRATION;

    @Value("${jwt.refresh.expiration}")
    public Long REFRESH_TOKEN_EXPIRATION;

    @Value("${app.frontend.base-url:https://hpground.xyz}")
    private String frontendBaseUrl;

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String oauthId = extractOauthId(oAuth2User.getAttributes());

        if (oauthId == null) {
            log.error("OAuth account null");
            sendOAuthMessageToOpener(response, "oauthError", "OAuth account ID missing.");
            return;
        }

        User user = userRepository.findByAccount(oauthId);
        if (user == null) {
            log.error("user is null");
            sendOAuthMessageToOpener(response, "oauthError", "User not registered. Please sign up first.");
            return;
        }

        String accessToken = jwtUtil.createJwt(user.getId(), user.getNickname(), user.getRole().name(), ACCESS_TOKEN_EXPIRATION);
        String refreshToken = jwtUtil.createJwt(user.getId(), user.getNickname(), user.getRole().name(), REFRESH_TOKEN_EXPIRATION);

        log.info("사용자가 로그인을 시도합니다. " + user.getNickname() + "의 토큰은 " + accessToken);

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) (REFRESH_TOKEN_EXPIRATION / 1000));
        refreshCookie.setAttribute("SameSite", "Lax");
        response.addCookie(refreshCookie);

        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> data = new HashMap<>();
        data.put("type", "oauthSuccess");
        data.put("accessToken", accessToken);
        data.put("nickname", user.getNickname());
        data.put("membershipId", String.valueOf(user.getId()));
        data.put("account", user.getAccount());

        String jsonData = objectMapper.writeValueAsString(data);

        response.getWriter().write("<!DOCTYPE html><html><head><title>Login Success</title></head><body>");
        response.getWriter().write("<script>");
        response.getWriter().write("if (window.opener) {");
        response.getWriter().write("  window.opener.postMessage(" + jsonData + ", '" + frontendBaseUrl + "');");
        response.getWriter().write("  window.close();");
        response.getWriter().write("} else {");
        response.getWriter().write("  window.location.href = '" + frontendBaseUrl + "?token=" + accessToken + "';");
        response.getWriter().write("}");
        response.getWriter().write("</script>");
        response.getWriter().write("</body></html>");
        response.getWriter().flush();
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

    private void sendOAuthMessageToOpener(HttpServletResponse response, String type, String errorMessage) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> errorData = new HashMap<>();
        errorData.put("type", type);
        errorData.put("error", errorMessage);

        String jsonData = objectMapper.writeValueAsString(errorData);

        response.getWriter().write("<!DOCTYPE html><html><head><title>Login Failed</title></head><body>");
        response.getWriter().write("<script>");
        response.getWriter().write("if (window.opener) {");
        response.getWriter().write("  window.opener.postMessage(" + jsonData + ", '" + frontendBaseUrl + "');");
        response.getWriter().write("  window.close();");
        response.getWriter().write("} else {");
        response.getWriter().write("  window.location.href = '" + frontendBaseUrl + "/login?error=" + errorMessage + "';"); // fallback
        response.getWriter().write("}");
        response.getWriter().write("</script>");
        response.getWriter().write("</body></html>");
        response.getWriter().flush();
    }
}

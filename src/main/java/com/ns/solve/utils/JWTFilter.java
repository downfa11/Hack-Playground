package com.ns.solve.utils;


import com.ns.solve.domain.entity.user.User;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.utils.exception.ErrorCode.UserErrorCode;
import com.ns.solve.utils.exception.SolvedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization= request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.split(" ")[1];

        if (jwtUtil.isExpired(token)) {
            log.info("만료된 토큰입니다.");
            filterChain.doFilter(request, response);
            return;
        }

        String nickname = jwtUtil.getNickname(token);

        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new SolvedException(UserErrorCode.USER_NOT_FOUND));

        if (user == null) {
            log.info("해당하는 사용자가 없습니다. : " + nickname);
            filterChain.doFilter(request, response);
            return;
        }

        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}

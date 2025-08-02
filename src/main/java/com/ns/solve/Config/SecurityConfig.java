package com.ns.solve.config;

import com.ns.solve.repository.UserRepository;
import com.ns.solve.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/reissue", "/token-validate").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/problems/statistics", "/api/problems/completed", "/api/boards", "/api/users/sorted-by-score").permitAll()
                        .requestMatchers("/api/admin/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs", "/v3/api-docs/**", "/api/k8s/**").hasRole("ADMIN")
                        .requestMatchers("/actuator/**").permitAll() // todo authenticated() 하고싶은데 아직 TLS 설정 해결중
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.disable())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))
                .oauth2Login(oauth2 -> oauth2.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(customOAuth2SuccessHandler))
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .addFilterBefore(new JWTFilter(userRepository,jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers
                                .cacheControl(cache -> cache.disable()) // Cache-Control: no-cache, no-store, max-age=0, must-revalidate, Pragma: no-cache, Expires: 0와 유사하게 동작
                                .frameOptions(frameOptions -> frameOptions.deny()) // X-Frame-Options: DENY 클릭재킹 방지 - 모든 프레임 포함 금지, sameOrigin()은 같은 도메인 내에서만 허용
                                .contentTypeOptions(Customizer.withDefaults()) // X-Content-Type-Options: nosniff MIME-Type 스니핑 방지
                                .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)) // X-XSS-Protection: 1; mode=block (구형 XSS 필터 사용)
                        )
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception { return configuration.getAuthenticationManager() ;}

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = corsConfigurationSource();
        return new CorsFilter(source);
    }
}



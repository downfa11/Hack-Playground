package com.ns.solve.controller;

import com.ns.solve.domain.dto.user.JWtToken;
import com.ns.solve.domain.dto.user.LoginUserRequest;
import com.ns.solve.domain.dto.user.ValidateTokenRequest;
import com.ns.solve.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;


    @PostMapping("/login")
    public ResponseEntity<JWtToken> loginMembership(@RequestBody LoginUserRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getAccount(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            JWtToken token = authService.login(request);
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PostMapping("/reissue")
    public ResponseEntity<JWtToken> reissue(@RequestBody String refreshToken) {
        JWtToken tokens = authService.reissue(refreshToken);
        if (tokens == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(tokens);
    }


    @PostMapping(path="/token-validate")
    boolean validateToken(@RequestBody ValidateTokenRequest request){
        return authService.validateJwtToken(request.getJwtToken());
    }
}

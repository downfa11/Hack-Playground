package com.ns.solve.utils;

import com.ns.solve.domain.entity.Role;
import com.ns.solve.domain.entity.User;
import com.ns.solve.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        log.warn("🔥🔥🔥 loadUser triggered!");

        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String provider = userRequest.getClientRegistration().getRegistrationId();
        User user = handleUser(provider, attributes);

        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                attributes,
                nameAttributeKey
        );
    }


    private User handleUser(String provider, Map<String, Object> userAttributes) {
        String oauthId = null;
        String nickname = null;

        if ("google".equals(provider)) {
            oauthId = (String) userAttributes.get("sub");
            nickname = (String) userAttributes.get("name");
        } else if ("github".equals(provider)) {
            oauthId = String.valueOf(userAttributes.get("id"));
            nickname = (String) userAttributes.get("login");
        }

        User user = userRepository.findByAccount(oauthId);
        if (user == null) {
            return registerNewOAuthUser(oauthId, nickname, provider);
        } else {
            return updateNicknameIfChanged(user, nickname);
        }
    }

    private User registerNewOAuthUser(String oauthId, String nickname, String provider) {
        User user = new User();
        user.setAccount(oauthId);
        user.setNickname(nickname);
        user.setPassword("");
        user.setRole(Role.ROLE_MEMBER);
        user.setScore(0L);
        user.setCreated(LocalDateTime.now());
        user.setLastActived(LocalDateTime.now());
        user.setProvider(provider);

        log.info("OAuth 사용자 {} 생성됨 (201)", nickname);
        return userRepository.save(user);
    }

    private User updateNicknameIfChanged(User user, String newNickname) {
        if (!user.getNickname().equals(newNickname)) {
            log.info("OAuth 사용자 닉네임 갱신됨: {} -> {}", user.getNickname(), newNickname);
            user.setNickname(newNickname);
            user.setLastActived(LocalDateTime.now());
            userRepository.save(user);
        }
        return user;
    }
}
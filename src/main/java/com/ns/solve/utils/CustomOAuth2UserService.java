package com.ns.solve.utils;

import com.ns.solve.domain.entity.user.Role;
import com.ns.solve.domain.entity.user.User;
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
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
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

        Optional<User> existingOAuthUser = userRepository.findByAccountAndProvider(oauthId, provider);

        if (existingOAuthUser.isPresent()) {
            User user = existingOAuthUser.get();
            log.info("기존 OAuth 사용자 (account: {}, provider: {}) 로그인, 닉네임 유지: {}", user.getAccount(), user.getProvider(), user.getNickname());
            user.setLastActived(LocalDateTime.now());
            return userRepository.save(user);
        } else {
            return registerNewOAuthUser(oauthId, nickname, provider);
        }
    }

    private User registerNewOAuthUser(String oauthId, String socialNickname, String provider) {
        String finalNickname = socialNickname;

        Optional<User> existingUserWithSameNickname = userRepository.findByNickname(socialNickname);

        if (existingUserWithSameNickname.isPresent() && "default".equals(existingUserWithSameNickname.get().getProvider())) {
            finalNickname = socialNickname + "_" + provider;
            log.info("닉네임 '{}'이(가) 'default'에서 이미 존재하므로 '{}'으로 변경", socialNickname, finalNickname);
        } else if (existingUserWithSameNickname.isPresent()) {
            finalNickname = socialNickname + "_" + provider;
            log.warn("닉네임 '{}'이(가) default 말고 다른 provider로 이미 존재합니다. 충돌 가능성: {}", socialNickname, existingUserWithSameNickname.get().getProvider());
        }


        User user = new User();
        user.setAccount(oauthId);
        user.setNickname(finalNickname);
        user.setPassword("");
        user.setRole(Role.ROLE_MEMBER);
        user.setScore(0L);
        user.setCreated(LocalDateTime.now());
        user.setLastActived(LocalDateTime.now());
        user.setProvider(provider);

        log.info("새로운 OAuth 사용자 {} (account: {}) 생성됨 (201) with nickname: {}", socialNickname, oauthId, finalNickname);
        return userRepository.save(user);
    }
}
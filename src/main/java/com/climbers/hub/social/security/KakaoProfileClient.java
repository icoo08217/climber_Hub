package com.climbers.hub.social.security;

import com.climbers.hub.social.domain.SocialProvider;
import com.climbers.hub.social.service.SocialIdentity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.stereotype.Component;
import java.util.Map;
import static com.climbers.hub.social.service.SocialAccountService.failure;

@Component
public class KakaoProfileClient {
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    public SocialIdentity load(OAuth2UserRequest request) { return parse(delegate.loadUser(request).getAttributes()); }

    public SocialIdentity parse(Map<String, Object> attributes) {
        Object id = attributes.get("id");
        if (id == null || !id.toString().matches("[1-9][0-9]*")) throw failure("invalid_profile");
        Map<?, ?> account = attributes.get("kakao_account") instanceof Map<?, ?> value ? value : Map.of();
        Map<?, ?> profile = account.get("profile") instanceof Map<?, ?> value ? value : Map.of();
        String nickname = profile.get("nickname") instanceof String value ? value : null;
        String email = null;
        if (Boolean.TRUE.equals(account.get("is_email_valid")) && Boolean.TRUE.equals(account.get("is_email_verified"))
                && account.get("email") instanceof String value && value.length() <= 255) email = value;
        return new SocialIdentity(SocialProvider.KAKAO, id.toString(), nickname, email);
    }
}

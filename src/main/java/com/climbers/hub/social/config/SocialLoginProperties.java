package com.climbers.hub.social.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.oauth")
@Getter @Setter
public class SocialLoginProperties {
    private String frontendBaseUrl = "http://localhost:5173";
    private Kakao kakao = new Kakao();

    @Getter @Setter
    public static class Kakao {
        private boolean enabled;
        private String clientId;
        private String clientSecret;
        private String redirectUri = "http://localhost:5173/api/login/oauth2/code/kakao";
    }
}

package com.climbers.hub.social.config;

import com.climbers.hub.social.security.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.*;
import org.springframework.security.oauth2.client.registration.*;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.*;
import org.springframework.util.Assert;

@Configuration
@ConditionalOnProperty(prefix = "app.oauth.kakao", name = "enabled", havingValue = "true")
public class KakaoOAuthConfiguration {
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(SocialLoginProperties properties) {
        var kakao = properties.getKakao();
        Assert.hasText(kakao.getClientId(), "Kakao REST API client-id is required");
        Assert.hasText(kakao.getClientSecret(), "Kakao client-secret is required");
        var frontend = java.net.URI.create(properties.getFrontendBaseUrl());
        Assert.isTrue(frontend.isAbsolute() && frontend.getHost() != null && frontend.getRawQuery() == null
                && frontend.getRawFragment() == null && frontend.getUserInfo() == null
                && ("http".equals(frontend.getScheme()) || "https".equals(frontend.getScheme()))
                && (frontend.getPath().isEmpty() || "/".equals(frontend.getPath())), "frontend-base-url must be an HTTP(S) origin");
        return new InMemoryClientRegistrationRepository(ClientRegistration.withRegistrationId("kakao")
                .clientId(kakao.getClientId()).clientSecret(kakao.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(kakao.getRedirectUri()).scope("profile_nickname")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me").userNameAttributeName("id")
                .clientName("Kakao").build());
    }

    @Bean
    public SocialAuthorizationRequests socialAuthorizationRequests(ClientRegistrationRepository registrations, LinkIntents intents) {
        return new SocialAuthorizationRequests(registrations, intents);
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> kakaoTokenClient() {
        return new RestClientAuthorizationCodeTokenResponseClient();
    }

    @Bean
    public OAuth2AuthorizedClientRepository transientAuthorizedClients() {
        // Login only: discard provider access/refresh tokens after profile lookup.
        return new OAuth2AuthorizedClientRepository() {
            @Override public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String id, Authentication principal, HttpServletRequest request) { return null; }
            @Override public void saveAuthorizedClient(OAuth2AuthorizedClient client, Authentication principal, HttpServletRequest request, HttpServletResponse response) {}
            @Override public void removeAuthorizedClient(String id, Authentication principal, HttpServletRequest request, HttpServletResponse response) {}
        };
    }
}

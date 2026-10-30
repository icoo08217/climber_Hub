package com.climbers.hub.config;

import com.climbers.hub.member.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import com.climbers.hub.social.config.SocialLoginProperties;
import com.climbers.hub.social.security.SocialAuthorizationRequests;
import com.climbers.hub.social.security.SocialOAuth2UserService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.endpoint.*;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(CustomUserDetailsService users, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public HttpSessionCsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository contexts,
                                         HttpSessionCsrfTokenRepository csrfTokens,
                                         SocialLoginProperties social,
                                         SocialOAuth2UserService socialUsers,
                                         ObjectProvider<SocialAuthorizationRequests> authorizationRequests,
                                         ObjectProvider<OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>> tokenClient,
                                         ObjectProvider<OAuth2AuthorizedClientRepository> authorizedClients) throws Exception {
        http.cors(Customizer.withDefaults())
                .httpBasic(basic -> basic.disable()).formLogin(form -> form.disable())
                .csrf(csrf -> csrf.csrfTokenRepository(csrfTokens))
                .requestCache(cache -> cache.disable())
                .securityContext(context -> context.securityContextRepository(contexts).requireExplicitSave(true))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/csrf", "/api/auth/signup", "/api/auth/login", "/error").permitAll()
                        .requestMatchers("/api/auth/social/providers", "/api/oauth2/authorization/**", "/api/login/oauth2/code/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/member/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> response.sendError(401))
                        .accessDeniedHandler((request, response, exception) -> response.sendError(403)))
                .logout(logout -> logout.logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, auth) -> response.setStatus(HttpServletResponse.SC_NO_CONTENT)));
        if (social.getKakao().isEnabled()) {
            var flow = authorizationRequests.getObject();
            String frontend = social.getFrontendBaseUrl().replaceAll("/$", "");
            http.oauth2Login(oauth -> oauth
                    .withObjectPostProcessor(new org.springframework.security.config.ObjectPostProcessor<org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter>() {
                        @Override public <O extends org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter> O postProcess(O filter) {
                            filter.setAuthenticationFailureHandler((request, response, exception) -> response.sendError(400));
                            return filter;
                        }
                    })
                    .authorizationEndpoint(endpoint -> endpoint.baseUri("/api/oauth2/authorization")
                            .authorizationRequestResolver(flow).authorizationRequestRepository(flow))
                    .redirectionEndpoint(endpoint -> endpoint.baseUri("/api/login/oauth2/code/*"))
                    .tokenEndpoint(endpoint -> endpoint.accessTokenResponseClient(tokenClient.getObject()))
                    .userInfoEndpoint(endpoint -> endpoint.userService(socialUsers))
                    .authorizedClientRepository(authorizedClients.getObject())
                    .successHandler((request, response, auth) -> response.sendRedirect(frontend + "/?social=success"))
                    .failureHandler((request, response, exception) -> {
                        String code = exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException error
                                ? error.getError().getErrorCode() : "login_failed";
                        if (!java.util.Set.of("email_conflict", "account_already_linked", "provider_already_linked", "link_session_expired", "account_conflict").contains(code)) code = "login_failed";
                        boolean linking = request.getAttribute(SocialAuthorizationRequests.LINK_MEMBER) != null;
                        response.sendRedirect(frontend + (linking ? "/?social_error=" : "/login?social_error=") + code);
                    }));
        }
        return http.build();
    }
}

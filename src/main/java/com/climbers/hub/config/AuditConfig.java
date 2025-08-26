package com.climbers.hub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/*
추후 Spring Security 추가한 후에, 얘를 사용하도록 개발해야함.
 */
@Configuration
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            // Spring Security 의 SecurityContextHolder 를 통해 현재 인증된 사용자 정보를 가져옴.
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 인증 정보가 없거나, 익명의 사용자인 경우 null 을 반환
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                return Optional.empty();
            }

            // 인증된 사용자의 이름 or ID 반환
            return Optional.of(authentication.getName());
        };
    }
}

package com.climbers.hub.social.controller;

import com.climbers.hub.auth.MemberPrincipal;
import com.climbers.hub.social.config.SocialLoginProperties;
import com.climbers.hub.social.security.LinkIntents;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/social")
@RequiredArgsConstructor
public class SocialLoginController {
    private final SocialLoginProperties properties;
    private final LinkIntents intents;

    @GetMapping("/providers")
    public Map<String, Boolean> providers() { return Map.of("kakao", properties.getKakao().isEnabled()); }

    @PostMapping("/kakao/link")
    public Map<String, String> link(@AuthenticationPrincipal MemberPrincipal principal, HttpServletRequest request) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (!properties.getKakao().isEnabled()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        return Map.of("authorizationUrl", "/api/oauth2/authorization/kakao?linkToken=" + intents.issue(request, principal));
    }
}

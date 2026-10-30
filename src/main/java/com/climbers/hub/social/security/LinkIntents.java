package com.climbers.hub.social.security;

import com.climbers.hub.auth.MemberPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import static com.climbers.hub.social.service.SocialAccountService.failure;

/** A CSRF-protected POST creates a short-lived, one-use link intent bound to this session. */
@Component
public class LinkIntents {
    private static final String KEY = LinkIntents.class.getName();
    private record Intent(String token, Long memberId, Instant expiresAt) implements Serializable {}

    public String issue(HttpServletRequest request, MemberPrincipal principal) {
        String token = UUID.randomUUID().toString();
        request.getSession().setAttribute(KEY, new Intent(token, principal.getMemberId(), Instant.now().plusSeconds(300)));
        return token;
    }

    public Long consume(HttpServletRequest request, String token) {
        var session = request.getSession(false);
        var intent = session == null ? null : (Intent) session.getAttribute(KEY);
        if (intent == null || !intent.token().equals(token) || !intent.expiresAt().isAfter(Instant.now())) throw failure("link_session_expired");
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof MemberPrincipal principal)
                || !principal.getMemberId().equals(intent.memberId())) throw failure("link_session_expired");
        session.removeAttribute(KEY);
        return intent.memberId();
    }
}

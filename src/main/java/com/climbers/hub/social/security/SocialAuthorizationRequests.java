package com.climbers.hub.social.security;

import com.climbers.hub.auth.MemberPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.*;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import java.time.Instant;
import static com.climbers.hub.social.service.SocialAccountService.failure;

public class SocialAuthorizationRequests implements OAuth2AuthorizationRequestResolver,
        AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    public static final String LINK_MEMBER = "social.linkMemberId";
    private static final String CREATED = "social.createdAt";
    private final DefaultOAuth2AuthorizationRequestResolver resolver;
    private final HttpSessionOAuth2AuthorizationRequestRepository repository = new HttpSessionOAuth2AuthorizationRequestRepository();
    private final LinkIntents linkIntents;

    public SocialAuthorizationRequests(ClientRegistrationRepository registrations, LinkIntents linkIntents) {
        resolver = new DefaultOAuth2AuthorizationRequestResolver(registrations, "/api/oauth2/authorization");
        this.linkIntents = linkIntents;
    }
    @Override public OAuth2AuthorizationRequest resolve(HttpServletRequest request) { return bind(request, resolver.resolve(request)); }
    @Override public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String id) { return bind(request, resolver.resolve(request, id)); }

    private OAuth2AuthorizationRequest bind(HttpServletRequest request, OAuth2AuthorizationRequest authorization) {
        if (authorization == null) return null;
        String ticket = request.getParameter("linkToken");
        Long memberId = ticket == null ? null : linkIntents.consume(request, ticket);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (memberId == null && authentication != null && authentication.getPrincipal() instanceof MemberPrincipal) throw failure("link_required");
        return OAuth2AuthorizationRequest.from(authorization).attributes(attributes -> {
            attributes.put(CREATED, Instant.now().getEpochSecond());
            if (memberId != null) attributes.put(LINK_MEMBER, memberId);
        }).build();
    }
    @Override public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) { return repository.loadAuthorizationRequest(request); }
    @Override public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorization, HttpServletRequest request, HttpServletResponse response) {
        repository.saveAuthorizationRequest(authorization, request, response);
    }
    @Override public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        // The delegate returns a request only if the session-bound OAuth state matches.
        var authorization = repository.removeAuthorizationRequest(request, response);
        if (authorization != null) {
            Long created = authorization.getAttribute(CREATED);
            if (created == null || Instant.now().getEpochSecond() - created > 300) throw failure("authorization_expired");
            Long memberId = authorization.getAttribute(LINK_MEMBER);
            if (memberId != null) request.setAttribute(LINK_MEMBER, memberId);
        }
        return authorization;
    }
}

package com.climbers.hub.social.security;

import com.climbers.hub.auth.MemberPrincipal;
import com.climbers.hub.social.service.SocialAccountService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import static com.climbers.hub.social.service.SocialAccountService.failure;

@Service
@RequiredArgsConstructor
public class SocialOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final KakaoProfileClient kakao;
    private final SocialAccountService accounts;
    private final ObjectProvider<HttpServletRequest> requests;

    @Override public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        if (!"kakao".equals(userRequest.getClientRegistration().getRegistrationId())) throw failure("unsupported_provider");
        var request = requests.getObject();
        Long target = (Long) request.getAttribute(SocialAuthorizationRequests.LINK_MEMBER);
        if (target != null) {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof MemberPrincipal principal)
                    || !principal.getMemberId().equals(target)) throw failure("link_session_expired");
        }
        try {
            return MemberPrincipal.social(accounts.loginOrLink(kakao.load(userRequest), target));
        } catch (DataIntegrityViolationException exception) {
            // Includes concurrent callbacks racing on the provider identity's unique key.
            throw failure("account_conflict");
        }
    }
}

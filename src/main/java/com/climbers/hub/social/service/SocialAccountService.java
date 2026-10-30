package com.climbers.hub.social.service;

import com.climbers.hub.member.domain.Member;
import com.climbers.hub.member.repository.MemberRepository;
import com.climbers.hub.social.domain.SocialAccount;
import com.climbers.hub.social.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SocialAccountService {
    private final MemberRepository members;
    private final SocialAccountRepository accounts;

    @Transactional
    public Member loginOrLink(SocialIdentity identity, Long linkMemberId) {
        if (identity.subject() == null || identity.subject().isBlank() || identity.subject().length() > 255) {
            throw failure("invalid_profile");
        }
        var existing = accounts.findByProviderAndProviderUserId(identity.provider(), identity.subject());
        if (existing.isPresent()) {
            // Resolve inside this transaction; OAuth filters run before MVC's OpenEntityManagerInView.
            Member owner = members.findById(existing.get().getMember().getMemberId())
                    .orElseThrow(() -> failure("invalid_profile"));
            if (linkMemberId != null && !owner.getMemberId().equals(linkMemberId)) throw failure("account_already_linked");
            return owner;
        }
        Member member;
        if (linkMemberId != null) {
            member = members.findById(linkMemberId).orElseThrow(() -> failure("link_session_expired"));
            if (accounts.existsByMember_MemberIdAndProvider(linkMemberId, identity.provider())) throw failure("provider_already_linked");
        } else {
            // A matching email is never evidence that these accounts have the same owner.
            if (identity.verifiedEmail() != null && members.existsByEmail(identity.verifiedEmail())) throw failure("email_conflict");
            String name = identity.nickname() == null || identity.nickname().isBlank() ? "클라이머" : identity.nickname();
            if (name.length() > 50) name = name.substring(0, 50);
            member = members.save(Member.builder().name(name).email(identity.verifiedEmail()).build());
        }
        accounts.saveAndFlush(new SocialAccount(member, identity.provider(), identity.subject()));
        return member;
    }

    public static OAuth2AuthenticationException failure(String code) {
        return new OAuth2AuthenticationException(new OAuth2Error(code));
    }
}

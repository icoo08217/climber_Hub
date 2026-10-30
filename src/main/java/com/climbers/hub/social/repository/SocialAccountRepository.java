package com.climbers.hub.social.repository;

import com.climbers.hub.social.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);
    boolean existsByMember_MemberIdAndProvider(Long memberId, SocialProvider provider);
    List<SocialAccount> findByMember_MemberId(Long memberId);
}

package com.climbers.hub.auth;

import com.climbers.hub.member.domain.Member;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** The same session identity for local and social login. Never contains provider tokens. */
public final class MemberPrincipal implements UserDetails, OAuth2User, CredentialsContainer {
    @Serial private static final long serialVersionUID = 1L;
    private final Long memberId;
    private final String role;
    private String password;

    private MemberPrincipal(Member member, boolean local) {
        memberId = member.getMemberId();
        role = member.getRole().name();
        password = local ? member.getPassword() : null;
    }
    public static MemberPrincipal local(Member member) { return new MemberPrincipal(member, true); }
    public static MemberPrincipal social(Member member) { return new MemberPrincipal(member, false); }
    public Long getMemberId() { return memberId; }
    @Override public String getName() { return memberId.toString(); }
    @Override public String getUsername() { return getName(); }
    @Override public String getPassword() { return password; }
    @Override public void eraseCredentials() { password = null; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
    @Override public Map<String, Object> getAttributes() { return Map.of("memberId", memberId); }
}

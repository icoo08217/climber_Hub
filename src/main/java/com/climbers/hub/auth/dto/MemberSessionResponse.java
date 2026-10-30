package com.climbers.hub.auth.dto;
import com.climbers.hub.member.domain.Member;
import java.util.List;

public record MemberSessionResponse(Long memberId, String loginId, String name, String email, String role,
                                    boolean localLoginEnabled, List<String> linkedProviders) {
    public static MemberSessionResponse from(Member member) {
        return new MemberSessionResponse(member.getMemberId(), member.getLoginId(), member.getName(),
                member.getEmail(), member.getRole().name(), member.getPassword() != null, List.of());
    }
}

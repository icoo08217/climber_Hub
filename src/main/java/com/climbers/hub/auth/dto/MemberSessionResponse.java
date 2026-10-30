package com.climbers.hub.auth.dto;
import com.climbers.hub.member.domain.Member;

public record MemberSessionResponse(Long memberId, String loginId, String name, String email, String role) {
    public static MemberSessionResponse from(Member member) {
        return new MemberSessionResponse(member.getMemberId(), member.getLoginId(), member.getName(),
                member.getEmail(), member.getRole().name());
    }
}

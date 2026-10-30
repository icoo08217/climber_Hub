package com.climbers.hub.member.dto;

import com.climbers.hub.member.domain.Member;
import lombok.Getter;

public class MemberDto {

    // Member 정보 응답 DTO
    @Getter
    public static class MemberResponse {
        private Long memberId;
        private String loginId;
        private String name;
        private String email;

        public MemberResponse(Member member) {
            this.memberId = member.getMemberId();
            this.loginId = member.getLoginId();
            this.name = member.getName();
            this.email = member.getEmail();
        }
    }

    // Member 정보 변경 요청 DTO
    @Getter
    public static class MemberUpdateRequest {
        private String name;
        private String password;
    }

}

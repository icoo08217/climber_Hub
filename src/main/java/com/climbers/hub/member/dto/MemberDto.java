package com.climbers.hub.member.dto;

import com.climbers.hub.member.Member;
import lombok.Getter;

public class MemberDto {

    // Member 생성 요청 DTO
    @Getter
    public static class MemberCreateRequest {
        private String name;
        private String email;
        private String password;

        public Member toEntity() {
            return Member.builder()
                    .name(name)
                    .email(email)
                    .password(password) // 암호화 필요.
                    .build();
        }
    }

    // Member 정보 응답 DTO
    @Getter
    public static class MemberResponse {
        private Long memberId;
        private String name;
        private String email;

        public MemberResponse(Member member) {
            this.memberId = member.getMemberId();
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

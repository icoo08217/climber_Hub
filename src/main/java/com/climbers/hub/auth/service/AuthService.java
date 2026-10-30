package com.climbers.hub.auth.service;

import com.climbers.hub.auth.dto.MemberSessionResponse;
import com.climbers.hub.auth.dto.SignUpRequest;
import com.climbers.hub.member.domain.Member;
import com.climbers.hub.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signUp(SignUpRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.");
        }
        if (memberRepository.existsByLoginId(request.loginId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 ID입니다.");
        }
        if (memberRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
        Member member = Member.builder().loginId(request.loginId()).name(request.name())
                .email(request.email()).password(request.password()).build();
        member.encodePassword(passwordEncoder);
        return memberRepository.saveAndFlush(member).getMemberId();
    }

    @Transactional(readOnly = true)
    public MemberSessionResponse currentMember(String loginId) {
        return memberRepository.findByLoginId(loginId).map(MemberSessionResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "다시 로그인해주세요."));
    }
}

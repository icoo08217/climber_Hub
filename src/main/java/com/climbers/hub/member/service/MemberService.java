package com.climbers.hub.member.service;

import com.climbers.hub.config.jwt.JwtTokenDto;
import com.climbers.hub.config.jwt.JwtTokenProvider;
import com.climbers.hub.mapper.MemberMapper;
import com.climbers.hub.member.domain.Member;
import com.climbers.hub.member.dto.MemberDto;
import com.climbers.hub.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    // Member create
    @Transactional
    public Long createMember(MemberDto.MemberCreateRequest request) {
        if(memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 Email 입니다.");
        }

        Member newMemberInfo = request.toEntity();
        // member 비밀번호 암호화
        newMemberInfo.encodePassword(passwordEncoder);

        Member savedMember = memberRepository.save(newMemberInfo);
        return savedMember.getMemberId();
    }

    // Member All Read
    @Transactional(readOnly = true)
    public List<MemberDto.MemberResponse> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(MemberDto.MemberResponse::new)
                .collect(Collectors.toList());
    }

    // 1 Member Read
    @Transactional(readOnly = true)
    public MemberDto.MemberResponse getMember(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));
        return new MemberDto.MemberResponse(findMember);
    }

    @Transactional
    public void updateMember(Long memberId, MemberDto.MemberUpdateRequest request) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member Not Found."));
        log.info("회원 정보 수정 시작. memberId: {}" , memberId);

        // MapStruct Mapper를 사용하여 DTO의 null이 아닌 값만 member 객체에 반영
        memberMapper.updateFromDto(findMember, request);
    }

    // Login
    @Transactional
    public JwtTokenDto login(String email, String password) {
        // login id/pw 를 기반으로 Authentication 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);

        // 실제 검증
        // 이 부분이 실행될 때 CustomUserDetailsService의 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 인증 정보를 기반으로 JWT 토큰 생성
        return jwtTokenProvider.generateToken(authentication);
    }
}

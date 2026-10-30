package com.climbers.hub.member.service;

import com.climbers.hub.member.domain.Member;
import com.climbers.hub.member.dto.MemberDto;
import com.climbers.hub.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final PasswordEncoder passwordEncoder;

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

        // 변경 메서드를 통해 반영하며 비밀번호는 반드시 해싱한다.
        findMember.updateProfile(request.getName(), request.getPassword(), passwordEncoder);
    }

}

package com.climbers.hub.member.service;

import com.climbers.hub.member.Member;
import com.climbers.hub.member.dto.MemberDto;
import com.climbers.hub.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    // Member create
    @Transactional
    public Long createMember(MemberDto.MemberCreateRequest request) {
        Member newMemberInfo = request.toEntity();
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
}

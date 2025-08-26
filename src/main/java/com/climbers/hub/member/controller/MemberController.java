package com.climbers.hub.member.controller;

import com.climbers.hub.member.dto.MemberDto;
import com.climbers.hub.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "회원 관리 api" , description = "회원 가입, 조회 등 회원 관련 api 명세")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    // Member create
    @Operation(summary = "신규 회원 가입")
    @PostMapping
    public ResponseEntity<Long> createMember(@RequestBody MemberDto.MemberCreateRequest request) {
        Long memberId = memberService.createMember(request);
        log.info("회원 가입 성공 : {}" , memberId);
        return ResponseEntity.ok(memberId);
    }

    @Operation(summary = "모든 회원 조회")
    @GetMapping("/list")
    public ResponseEntity<List<MemberDto.MemberResponse>> getAllMembers() {
        List<MemberDto.MemberResponse> memberList = memberService.getAllMembers();
        log.info("모든 회원 조회 API 호출");
        return ResponseEntity.ok(memberList);
    }

    @Operation(summary = "특정 회원 조회")
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberDto.MemberResponse> getMember(@PathVariable Long memberId) {
        MemberDto.MemberResponse findMember = memberService.getMember(memberId);
        log.info("특정 회원 조회 API 호출: {}" , findMember.getMemberId());
        return ResponseEntity.ok(findMember);
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<Void> updateMember(@PathVariable Long memberId, @RequestBody MemberDto.MemberUpdateRequest request) {
        memberService.updateMember(memberId, request);
        return ResponseEntity.ok().build();
    }
}

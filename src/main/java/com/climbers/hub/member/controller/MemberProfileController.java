package com.climbers.hub.member.controller;

import com.climbers.hub.auth.MemberPrincipal;
import com.climbers.hub.auth.dto.MemberSessionResponse;
import com.climbers.hub.auth.service.AuthService;
import com.climbers.hub.member.dto.PasswordChangeRequest;
import com.climbers.hub.member.dto.ProfileUpdateRequest;
import com.climbers.hub.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/members/me")
@RequiredArgsConstructor
public class MemberProfileController {
    private final MemberService memberService;
    private final AuthService authService;

    @PatchMapping("/profile")
    public MemberSessionResponse updateProfile(Authentication authentication,
                                               @Valid @RequestBody ProfileUpdateRequest request) {
        MemberPrincipal principal = requirePrincipal(authentication);
        memberService.updateOwnProfile(principal.getMemberId(), request);
        return authService.currentMember(principal);
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
                                               @Valid @RequestBody PasswordChangeRequest request,
                                               HttpServletRequest servletRequest) {
        MemberPrincipal principal = requirePrincipal(authentication);
        memberService.changeOwnPassword(principal.getMemberId(), request);
        servletRequest.changeSessionId();
        return ResponseEntity.noContent().build();
    }

    private MemberPrincipal requirePrincipal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof MemberPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "다시 로그인해주세요.");
        }
        return principal;
    }
}

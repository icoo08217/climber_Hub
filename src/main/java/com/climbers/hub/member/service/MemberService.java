package com.climbers.hub.member.service;

import com.climbers.hub.member.domain.Member;
import com.climbers.hub.member.dto.MemberDto;
import com.climbers.hub.member.dto.PasswordChangeRequest;
import com.climbers.hub.member.dto.ProfileUpdateRequest;
import com.climbers.hub.member.repository.MemberRepository;
import com.climbers.hub.media.service.MediaStorage;
import com.climbers.hub.media.service.StoredMedia;
import com.climbers.hub.post.domain.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MediaStorage mediaStorage;

    private static final Set<String> PROFILE_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long PROFILE_IMAGE_MAX_SIZE = 5L * 1024 * 1024;

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

    @Transactional
    public void updateOwnProfile(Long memberId, ProfileUpdateRequest request) {
        Member member = requireMember(memberId);
        String name = request.name().trim();
        String email = request.email() == null || request.email().isBlank()
                ? null : request.email().trim().toLowerCase(Locale.ROOT);
        if (email != null && memberRepository.existsByEmailAndMemberIdNot(email, memberId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
        member.updateOwnProfile(name, email);
    }

    @Transactional
    public void changeOwnPassword(Long memberId, PasswordChangeRequest request) {
        Member member = requireMember(memberId);
        if (member.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "일반 로그인 비밀번호가 설정되지 않은 계정입니다.");
        }
        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다.");
        }
        if (request.newPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "새 비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.");
        }
        if (passwordEncoder.matches(request.newPassword(), member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }
        member.changePassword(request.newPassword(), passwordEncoder);
    }

    @Transactional
    public void updateProfileImage(Long memberId, MultipartFile file) {
        validateProfileImage(file);
        Member member = requireMember(memberId);
        StoredMedia uploaded = mediaStorage.uploadProfileImage(file);
        String previousPublicId = member.getProfileImagePublicId();
        registerProfileImageCleanup(uploaded.publicId(), previousPublicId);
        member.updateProfileImage(uploaded.secureUrl(), uploaded.publicId());
    }

    @Transactional
    public void deleteProfileImage(Long memberId) {
        Member member = requireMember(memberId);
        String previousPublicId = member.getProfileImagePublicId();
        if (previousPublicId == null) return;
        member.clearProfileImage();
        registerDeleteAfterCommit(previousPublicId);
    }

    private void validateProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "프로필 사진을 선택해주세요.");
        }
        if (!PROFILE_IMAGE_TYPES.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JPG, PNG, WEBP, GIF 이미지만 올릴 수 있습니다.");
        }
        if (file.getSize() > PROFILE_IMAGE_MAX_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "프로필 사진은 5MB 이하여야 합니다.");
        }
    }

    private void registerProfileImageCleanup(String uploadedPublicId, String previousPublicId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (previousPublicId != null) safeDeleteImage(previousPublicId);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) safeDeleteImage(uploadedPublicId);
            }
        });
    }

    private void registerDeleteAfterCommit(String publicId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                safeDeleteImage(publicId);
            }
        });
    }

    private void safeDeleteImage(String publicId) {
        try {
            mediaStorage.delete(publicId, MediaType.IMAGE);
        } catch (RuntimeException exception) {
            log.warn("프로필 이미지 정리에 실패했습니다. publicId={}", publicId, exception);
        }
    }

    private Member requireMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "다시 로그인해주세요."));
    }

}

package com.climbers.hub.auth.service;

import com.climbers.hub.auth.dto.LoginRequest;
import com.climbers.hub.auth.dto.SignUpRequest;
import com.climbers.hub.config.jwt.JwtTokenDto;
import com.climbers.hub.member.domain.Member;
import com.climbers.hub.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 성공 - 비밀번호가 암호화되어 저장되어야 한다")
    void signUpSuccess() {
        // given
        SignUpRequest request = new SignUpRequest();
        createSignUpRequest(request, "test@naver.com", "1234", "김클밍");

        // when
        Long memberId = authService.signUp(request);

        // then
        Member savedMember = memberRepository.findById(memberId).orElseThrow();

        assertThat(savedMember.getEmail()).isEqualTo("test@naver.com");
        assertThat(savedMember.getName()).isEqualTo("김클밍");
        assertThat(savedMember.getPassword()).isNotEqualTo("1234"); // 비밀번호 암호화 확인
        assertThat(passwordEncoder.matches("1234", savedMember.getPassword())).isTrue(); // 암호화된 비번 검증
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 이메일")
    void signUpDuplicateEmail() {
        // given
        SignUpRequest request1 = new SignUpRequest();
        createSignUpRequest(request1, "duplicate@naver.com", "1234", "유저1");
        authService.signUp(request1);

        SignUpRequest request2 = new SignUpRequest();
        createSignUpRequest(request2, "duplicate@naver.com", "5678", "유저2");

        // when & then
        assertThatThrownBy(() -> authService.signUp(request2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 가입된 이메일입니다.");
    }

    @Test
    @DisplayName("로그인 성공 - 토큰이 발급되어야 한다")
    void loginSuccess() {
        // given
        SignUpRequest signUpRequest = new SignUpRequest();
        createSignUpRequest(signUpRequest, "login@naver.com", "password123", "로그인유저");
        authService.signUp(signUpRequest);

        LoginRequest loginRequest = new LoginRequest();
        createLoginRequest(loginRequest, "login@naver.com", "password123");

        // when
        JwtTokenDto tokenDto = authService.login(loginRequest);

        // then
        assertThat(tokenDto).isNotNull();
        assertThat(tokenDto.getAccessToken()).isNotNull();
        System.out.println("Access Token: " + tokenDto.getAccessToken()); // 눈으로 확인용
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void loginFailWrongPassword() {
        // given
        SignUpRequest signUpRequest = new SignUpRequest();
        createSignUpRequest(signUpRequest, "fail@naver.com", "password123", "실패유저");
        authService.signUp(signUpRequest);

        LoginRequest loginRequest = new LoginRequest();
        createLoginRequest(loginRequest, "fail@naver.com", "wrongPassword");

        // when & then
        // Spring Security의 AuthenticationManager는 비번 틀리면 BadCredentialsException을 던짐
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    // --- 테스트용 헬퍼 메서드 (DTO에 Setter나 생성자가 없어서 리플렉션 사용) ---
    // 만약 DTO에 @Setter나 @AllArgsConstructor가 있다면 이걸 안 쓰고 바로 넣으시면 됩니다.
    private void createSignUpRequest(SignUpRequest request, String email, String password, String name) {
        try {
            setField(request, "email", email);
            setField(request, "password", password);
            setField(request, "name", name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createLoginRequest(LoginRequest request, String email, String password) {
        try {
            setField(request, "email", email);
            setField(request, "password", password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object object, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}
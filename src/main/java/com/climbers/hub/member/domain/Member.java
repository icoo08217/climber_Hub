package com.climbers.hub.member.domain;

import com.climbers.hub.domain.BaseEntity;
import com.climbers.hub.member.constant.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity @Getter
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB 에 primary_key 를 위임한다.
    private Long memberId;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public Member(String loginId, String name, String email, String password) {
        this.loginId = loginId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = Role.USER; // 기본 값은 USER 로 설정.
    }

    // password encoder
    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    public void updateProfile(String name, String password, PasswordEncoder encoder) {
        if (name != null && !name.isBlank()) this.name = name;
        if (password != null && !password.isBlank()) this.password = encoder.encode(password);
    }
}



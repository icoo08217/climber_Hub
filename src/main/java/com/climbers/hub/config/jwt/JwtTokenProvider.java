package com.climbers.hub.config.jwt;

/*
    jwt 생성하고, 들어온 jwt가 유효한지 검증하는 핵심 로직 담당 클래스
 */

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;

    // application.yml 파일에서 secret key 를 가져와서 key 객체 생성
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // member 정보를 가지고 accessToken, refreshToken을 생성하는 메서드
}

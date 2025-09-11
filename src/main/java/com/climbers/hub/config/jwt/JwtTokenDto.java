package com.climbers.hub.config.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class JwtTokenDto {
    private String grantType; // jwt에 대한 인증 타입.
    private String accessToken;
    private String refreshToken;
}

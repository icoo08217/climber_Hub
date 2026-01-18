package com.climbers.hub.auth.controller;

import com.climbers.hub.auth.dto.LoginRequest;
import com.climbers.hub.auth.dto.SignUpRequest;
import com.climbers.hub.auth.service.AuthService;
import com.climbers.hub.config.jwt.JwtTokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<Long> signUp(@RequestBody SignUpRequest request){
        return ResponseEntity.ok(authService.signUp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtTokenDto> login(@RequestBody LoginRequest request){
        JwtTokenDto jwtToken = authService.login(request);
        return ResponseEntity.ok(jwtToken);
    }
}

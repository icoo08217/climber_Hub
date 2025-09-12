package com.climbers.hub.member.controller;

import com.climbers.hub.member.dto.MemberDto;
import com.climbers.hub.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc // MockMvc를 사용하기 위한 어노테이션
@Transactional // 테스트 후 데이터를 롤백하기 위함
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc; // API를 테스트하기 위한 가짜 MVC 환경

    @Autowired
    private ObjectMapper objectMapper; // 객체를 JSON으로 변환하기 위함

    @Autowired
    private MemberService memberService;

    // 각 테스트가 실행되기 전에, 테스트용 회원을 미리 생성
    @BeforeEach
    void setUp() {
        MemberDto.MemberCreateRequest request = new MemberDto.MemberCreateRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setName("테스트유저");
        memberService.createMember(request);
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() throws Exception {
        // given: 로그인 요청 DTO 준비
        MemberDto.LoginRequest loginRequest = new MemberDto.LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
        String jsonRequest = objectMapper.writeValueAsString(loginRequest);

        // when: 로그인 API에 POST 요청
        mockMvc.perform(post("/member/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                // then: 응답 결과 검증
                .andExpect(status().isOk()) // 200 OK 상태인지 확인
                .andExpect(jsonPath("$.accessToken").isNotEmpty()) // accessToken이 비어있지 않은지 확인
                .andExpect(jsonPath("$.grantType").value("Bearer")) // grantType이 "Bearer"인지 확인
                .andDo(print()); // 요청/응답 내용 출력
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_fail_wrong_password() throws Exception {
        // given: 잘못된 비밀번호로 로그인 요청 DTO 준비
        MemberDto.LoginRequest loginRequest = new MemberDto.LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongpassword");
        String jsonRequest = objectMapper.writeValueAsString(loginRequest);

        // when: 로그인 API에 POST 요청
        mockMvc.perform(post("/member/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                // then: 4xx 클라이언트 에러가 발생하는지 확인 (인증 실패)
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }
}
package com.climbers.hub.auth;

import com.climbers.hub.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SessionAuthenticationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired MemberRepository members;
    @Autowired PasswordEncoder encoder;

    String signup(String id, String email) throws Exception {
        return json.writeValueAsString(Map.of("loginId", id, "email", email, "name", "클라이머", "password", "password123"));
    }

    void createMember() throws Exception {
        mvc.perform(post("/api/auth/signup").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(signup("climber95", "climber@example.com"))).andExpect(status().isCreated());
    }

    @Test
    void signupHashesPasswordAndRejectsDuplicateIdAndEmail() throws Exception {
        createMember();
        var member = members.findByLoginId("climber95").orElseThrow();
        assertThat(encoder.matches("password123", member.getPassword())).isTrue();
        mvc.perform(post("/api/auth/signup").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(signup("climber95", "other@example.com"))).andExpect(status().isConflict());
        mvc.perform(post("/api/auth/signup").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(signup("another_id", "climber@example.com"))).andExpect(status().isConflict());
    }

    @Test
    void loginPersistsSessionRotatesIdAndCsrfAndLogoutInvalidatesIt() throws Exception {
        createMember();
        MvcResult csrfResult = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) csrfResult.getRequest().getSession(false);
        String oldSessionId = session.getId();
        var csrfBody = json.readTree(csrfResult.getResponse().getContentAsString());
        String headerName = csrfBody.get("headerName").asText();
        String oldToken = csrfBody.get("token").asText();
        mvc.perform(post("/api/auth/login").session(session).header(headerName, oldToken)
                .contentType(MediaType.APPLICATION_JSON).content("{\"loginId\":\"climber95\",\"password\":\"password123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.loginId").value("climber95"))
                .andExpect(jsonPath("$.password").doesNotExist()).andExpect(jsonPath("$.accessToken").doesNotExist());
        assertThat(session.getId()).isNotEqualTo(oldSessionId);
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("climber@example.com"));
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/logout").session(session).header(headerName, oldToken)).andExpect(status().isForbidden());
        MvcResult newCsrf = mvc.perform(get("/api/auth/csrf").session(session)).andReturn();
        String newToken = json.readTree(newCsrf.getResponse().getContentAsString()).get("token").asText();
        mvc.perform(post("/api/auth/logout").session(session).header(headerName, newToken)).andExpect(status().isNoContent());
        assertThat(session.isInvalid()).isTrue();
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsEmailLoginWrongPasswordAndUnknownId() throws Exception {
        createMember();
        for (String[] attempt : new String[][]{{"climber@example.com", "password123"}, {"climber95", "wrong"}, {"unknown", "password123"}}) {
            mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(Map.of("loginId", attempt[0], "password", attempt[1]))))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void requiresCsrfAndValidSignupFields() throws Exception {
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content(signup("climber95", "climber@example.com"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"climber95\",\"password\":\"password123\"}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/signup").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(signup("BAD ID", "invalid"))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/signup").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("loginId", "valid_id", "name", "사용자", "email", "a@example.com", "password", "가".repeat(25)))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ordinaryMemberCannotReadOrModifyOtherMembers() throws Exception {
        createMember();
        MvcResult result = mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"climber95\",\"password\":\"password123\"}")).andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        mvc.perform(get("/member/list").session(session)).andExpect(status().isForbidden());
        mvc.perform(patch("/member/1").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"changed123\"}")).andExpect(status().isForbidden());
    }
}

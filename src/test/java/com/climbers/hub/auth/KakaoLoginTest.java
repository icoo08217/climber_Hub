package com.climbers.hub.auth;

import com.climbers.hub.auth.dto.SignUpRequest;
import com.climbers.hub.auth.service.AuthService;
import com.climbers.hub.member.repository.MemberRepository;
import com.climbers.hub.social.domain.SocialProvider;
import com.climbers.hub.social.repository.SocialAccountRepository;
import com.climbers.hub.social.security.KakaoProfileClient;
import com.climbers.hub.social.service.SocialIdentity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.oauth2.client.endpoint.*;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"app.oauth.kakao.enabled=true", "app.oauth.kakao.client-id=test-client", "app.oauth.kakao.client-secret=test-secret"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class KakaoLoginTest {
    @Autowired MockMvc mvc;
    @Autowired AuthService auth;
    @Autowired MemberRepository members;
    @Autowired SocialAccountRepository accounts;
    @Autowired ObjectMapper json;
    @Autowired com.climbers.hub.social.service.SocialAccountService socialService;
    @MockitoBean KakaoProfileClient kakao;
    @MockitoBean(name = "kakaoTokenClient") OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> tokens;

    record Flow(MockHttpSession session, String state) {}
    @BeforeEach void remoteResponses() {
        when(tokens.getTokenResponse(any())).thenReturn(OAuth2AccessTokenResponse.withToken("test-access-token")
                .tokenType(OAuth2AccessToken.TokenType.BEARER).expiresIn(3600).build());
        profile("123456", null);
    }
    void profile(String subject, String email) {
        when(kakao.load(any())).thenReturn(new SocialIdentity(SocialProvider.KAKAO, subject, "카카오클라이머", email));
    }
    Flow start(String url, MockHttpSession session) throws Exception {
        var request = get(url);
        if (session != null) request.session(session);
        var result = mvc.perform(request).andExpect(status().is3xxRedirection()).andReturn();
        String location = result.getResponse().getRedirectedUrl();
        assertThat(location).startsWith("https://kauth.kakao.com/oauth/authorize?");
        var params = UriComponentsBuilder.fromUriString(location).build().getQueryParams();
        assertThat(params.getFirst("scope")).isEqualTo("profile_nickname");
        assertThat(params.getFirst("redirect_uri")).isEqualTo("http://localhost:5173/api/login/oauth2/code/kakao");
        return new Flow((MockHttpSession) result.getRequest().getSession(false),
                org.springframework.web.util.UriUtils.decode(params.getFirst("state"), java.nio.charset.StandardCharsets.UTF_8));
    }
    MvcResult callback(Flow flow) throws Exception {
        return mvc.perform(get("/api/login/oauth2/code/kakao").session(flow.session())
                .param("code", "test-code").param("state", flow.state())).andExpect(status().is3xxRedirection()).andReturn();
    }
    MockHttpSession localSession() throws Exception {
        auth.signUp(new SignUpRequest("local_user", "로컬회원", "local@example.com", "password123"));
        return (MockHttpSession) mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"local_user\",\"password\":\"password123\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }
    String linkUrl(MockHttpSession session) throws Exception {
        var result = mvc.perform(post("/api/auth/social/kakao/link").session(session).with(csrf()))
                .andExpect(status().isOk()).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("authorizationUrl").asText();
    }

    @Test void newKakaoMemberGetsSessionWithoutLocalCredentialsAndRepeatLoginFindsSameMember() throws Exception {
        Flow first = start("/api/oauth2/authorization/kakao", null);
        String previousId = first.session().getId();
        assertThat(callback(first).getResponse().getRedirectedUrl()).isEqualTo("http://localhost:5173/?social=success");
        assertThat(first.session().getId()).isNotEqualTo(previousId);
        mvc.perform(get("/api/auth/me").session(first.session())).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("카카오클라이머"))
                .andExpect(jsonPath("$.localLoginEnabled").value(false))
                .andExpect(jsonPath("$.linkedProviders[0]").value("KAKAO"));
        var member = members.findAll().get(0);
        assertThat(member.getLoginId()).isNull();
        assertThat(member.getPassword()).isNull();
        assertThat(member.getEmail()).isNull();
        callback(start("/api/oauth2/authorization/kakao", null));
        assertThat(members.count()).isEqualTo(1);
        assertThat(accounts.count()).isEqualTo(1);
        assertThat(first.session().getAttributeNames().asIterator()).toIterable().noneMatch(name -> name.contains("AUTHORIZED_CLIENT"));
    }

    @Test void existingMemberLinksKakaoAndSocialLoginKeepsTheSameInternalMember() throws Exception {
        var session = localSession();
        Long memberId = members.findByLoginId("local_user").orElseThrow().getMemberId();
        String url = linkUrl(session);
        callback(start(url, session));
        assertThat(members.count()).isEqualTo(1);
        assertThat(accounts.findAll().get(0).getMember().getMemberId()).isEqualTo(memberId);
        Flow social = start("/api/oauth2/authorization/kakao", null);
        callback(social);
        mvc.perform(get("/api/auth/me").session(social.session())).andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(memberId))
                .andExpect(jsonPath("$.loginId").value("local_user"))
                .andExpect(jsonPath("$.localLoginEnabled").value(true));
    }

    @Test void matchingEmailNeverAutomaticallyLinksOrCreatesASecondMember() throws Exception {
        localSession();
        profile("123456", "local@example.com");
        var result = callback(start("/api/oauth2/authorization/kakao", null));
        assertThat(result.getResponse().getRedirectedUrl()).endsWith("/login?social_error=email_conflict");
        assertThat(members.count()).isEqualTo(1);
        assertThat(accounts.count()).isZero();
    }

    @Test void rejectsWrongStateAndReplayedCallbackBeforeContactingProvider() throws Exception {
        Flow flow = start("/api/oauth2/authorization/kakao", null);
        mvc.perform(get("/api/login/oauth2/code/kakao").session(flow.session()).param("code", "x").param("state", "wrong"))
                .andExpect(status().is3xxRedirection());
        verifyNoInteractions(tokens, kakao);
        callback(flow);
        clearInvocations(tokens, kakao);
        callback(flow);
        verifyNoInteractions(tokens, kakao);
        assertThat(accounts.count()).isEqualTo(1);
    }

    @Test void linkingRequiresAuthenticationAndCsrf() throws Exception {
        mvc.perform(post("/api/auth/social/kakao/link").with(csrf())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/social/kakao/link").session(localSession())).andExpect(status().isForbidden());
    }

    @Test void refusesTakingAnotherMembersKakaoAccountAndPreservesOriginalLogin() throws Exception {
        callback(start("/api/oauth2/authorization/kakao", null));
        var local = localSession();
        var result = callback(start(linkUrl(local), local));
        assertThat(result.getResponse().getRedirectedUrl()).endsWith("/?social_error=account_already_linked");
        assertThat(accounts.count()).isEqualTo(1);
        mvc.perform(get("/api/auth/me").session(local)).andExpect(status().isOk()).andExpect(jsonPath("$.loginId").value("local_user"));
    }

    @Test void lostLinkSessionCannotSilentlyCreateANewAccount() throws Exception {
        var session = localSession();
        Flow flow = start(linkUrl(session), session);
        session.removeAttribute("SPRING_SECURITY_CONTEXT");
        var result = callback(flow);
        assertThat(result.getResponse().getRedirectedUrl()).endsWith("/?social_error=link_session_expired");
        assertThat(accounts.count()).isZero();
        assertThat(members.count()).isEqualTo(1);
    }

    @Test void denialCreatesNoMember() throws Exception {
        Flow flow = start("/api/oauth2/authorization/kakao", null);
        mvc.perform(get("/api/login/oauth2/code/kakao").session(flow.session()).param("state", flow.state()).param("error", "access_denied"))
                .andExpect(redirectedUrl("http://localhost:5173/login?social_error=login_failed"));
        verifyNoInteractions(tokens, kakao);
        assertThat(members.count()).isZero();
    }

    @Test void memberCannotLinkASecondKakaoAndLinkTicketIsSingleUse() throws Exception {
        var session = localSession();
        String ticketUrl = linkUrl(session);
        callback(start(ticketUrl, session));
        mvc.perform(get(ticketUrl).session(session)).andExpect(status().is4xxClientError());
        profile("654321", null);
        var result = callback(start(linkUrl(session), session));
        assertThat(result.getResponse().getRedirectedUrl()).endsWith("/?social_error=provider_already_linked");
        assertThat(accounts.count()).isEqualTo(1);
    }

    @Test void expiredAuthorizationRequestIsRejectedBeforeTokenExchange() throws Exception {
        Flow flow = start("/api/oauth2/authorization/kakao", null);
        var names = flow.session().getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (flow.session().getAttribute(name) instanceof org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest authorization) {
                flow.session().setAttribute(name, org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest.from(authorization)
                        .attributes(attributes -> attributes.put("social.createdAt", 0L)).build());
            }
        }
        assertThat(callback(flow).getResponse().getRedirectedUrl()).endsWith("/login?social_error=login_failed");
        verifyNoInteractions(tokens, kakao);
        assertThat(accounts.count()).isZero();
    }

    @Test void anotherBrowserCannotUseMembersLinkTicket() throws Exception {
        var session = localSession();
        String url = linkUrl(session);
        mvc.perform(get(url)).andExpect(status().isBadRequest());
        verifyNoInteractions(tokens, kakao);
        assertThat(accounts.count()).isZero();
        // The owner's browser can still consume its own ticket.
        callback(start(url, session));
        assertThat(accounts.count()).isEqualTo(1);
    }

    @Test
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void returningSocialLoginWorksAcrossIndependentDatabaseTransactions() throws Exception {
        try {
            assertThat(callback(start("/api/oauth2/authorization/kakao", null)).getResponse().getRedirectedUrl()).endsWith("/?social=success");
            assertThat(callback(start("/api/oauth2/authorization/kakao", null)).getResponse().getRedirectedUrl()).endsWith("/?social=success");
            assertThat(members.count()).isEqualTo(1);
        } finally {
            // This test runs only against the isolated application-test H2 database.
            accounts.deleteAll();
            members.deleteAll();
        }
    }

    @Test void futureProvidersCanLinkToTheSameMemberWithoutSharingTheirIdentityNamespace() {
        Long memberId = auth.signUp(new SignUpRequest("future_user", "확장회원", "future@example.com", "password123"));
        socialService.loginOrLink(new SocialIdentity(SocialProvider.NAVER, "same-subject", "네이버", null), memberId);
        socialService.loginOrLink(new SocialIdentity(SocialProvider.GOOGLE, "same-subject", "구글", null), memberId);
        assertThat(accounts.findByMember_MemberId(memberId)).hasSize(2);
        assertThat(socialService.loginOrLink(new SocialIdentity(SocialProvider.GOOGLE, "same-subject", "구글", null), null).getMemberId()).isEqualTo(memberId);
        assertThat(members.count()).isEqualTo(1);
    }
}

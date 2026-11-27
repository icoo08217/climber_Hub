package com.climbers.hub.gym;

import com.climbers.hub.gym.domain.Gym;
import com.climbers.hub.gym.repository.GymRepository;
import com.climbers.hub.member.domain.Member;
import com.climbers.hub.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GymOwnerAdminApiTest {
    @Autowired MockMvc mvc;
    @Autowired MemberRepository memberRepository;
    @Autowired GymRepository gymRepository;

    private Long memberId;
    private Long gymId;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(Member.builder()
                .loginId("owner_candidate")
                .name("암장 운영자")
                .email("owner@example.com")
                .password("not-used-in-this-test")
                .build());
        Gym gym = gymRepository.save(Gym.builder()
                .name("테스트 클라이밍장")
                .address("서울시 테스트구 1")
                .build());
        memberId = member.getMemberId();
        gymId = gym.getGymId();
    }

    @Test
    void onlyAdminCanOpenOwnerManagementApi() throws Exception {
        mvc.perform(get("/api/admin/gym-owners").with(user("member").roles("USER")))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/admin/gym-owners").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members[0].role").value("USER"))
                .andExpect(jsonPath("$.gyms[0].name").value("테스트 클라이밍장"))
                .andExpect(jsonPath("$.gyms[0].owners").isEmpty());
    }

    @Test
    void adminAssignsAndRemovesGymOwner() throws Exception {
        String request = "{\"memberId\":" + memberId + "}";
        mvc.perform(post("/api/admin/gyms/{gymId}/owners", gymId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(memberId))
                .andExpect(jsonPath("$.name").value("암장 운영자"));

        mvc.perform(post("/api/admin/gyms/{gymId}/owners", gymId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/admin/gym-owners").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gyms[0].owners[0].memberId").value(memberId));

        mvc.perform(delete("/api/admin/gyms/{gymId}/owners/{memberId}", gymId, memberId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/admin/gym-owners").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gyms[0].owners").isEmpty());
    }

    @Test
    void adminReplacesGymOwner() throws Exception {
        Member replacement = memberRepository.save(Member.builder()
                .loginId("replacement_manager")
                .name("새 관리인")
                .email("replacement@example.com")
                .password("not-used-in-this-test")
                .build());

        mvc.perform(post("/api/admin/gyms/{gymId}/owners", gymId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":" + memberId + "}"))
                .andExpect(status().isCreated());

        mvc.perform(patch("/api/admin/gyms/{gymId}/owners/{memberId}", gymId, memberId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newMemberId\":" + replacement.getMemberId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(replacement.getMemberId()))
                .andExpect(jsonPath("$.loginId").value("replacement_manager"));

        mvc.perform(get("/api/admin/gym-owners").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gyms[0].owners.length()").value(1))
                .andExpect(jsonPath("$.gyms[0].owners[0].memberId").value(replacement.getMemberId()));
    }
}

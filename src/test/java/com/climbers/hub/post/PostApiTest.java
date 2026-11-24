package com.climbers.hub.post;

import com.climbers.hub.media.service.MediaStorage;
import com.climbers.hub.media.service.StoredMedia;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PostApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @MockitoBean MediaStorage mediaStorage;

    private MockHttpSession member(String loginId, String email) throws Exception {
        mvc.perform(post("/api/auth/signup").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "loginId", loginId,
                        "email", email,
                        "name", loginId,
                        "password", "password123"))))
                .andExpect(status().isCreated());
        MvcResult login = mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("loginId", loginId, "password", "password123"))))
                .andExpect(status().isOk()).andReturn();
        return (MockHttpSession) login.getRequest().getSession(false);
    }

    @Test
    void globalBoardUsesSessionMemberAndProtectsAuthorChanges() throws Exception {
        MockHttpSession author = member("board_author", "author@example.com");
        MockHttpSession other = member("board_other", "other@example.com");

        mvc.perform(get("/api/posts")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/posts").session(author).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"첫 게시글\",\"content\":\"첫 내용\"}"))
                .andExpect(status().isForbidden());
        MvcResult created = mvc.perform(post("/api/posts").session(author).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\" 첫 게시글 \",\"content\":\" 첫 내용 \"}"))
                .andExpect(status().isCreated()).andReturn();
        long postId = Long.parseLong(created.getResponse().getContentAsString());

        mvc.perform(get("/api/posts").session(author))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].postId").value(postId))
                .andExpect(jsonPath("$[0].title").value("첫 게시글"))
                .andExpect(jsonPath("$[0].memberName").value("board_author"))
                .andExpect(jsonPath("$[0].postType").value("GLOBAL_POST"));
        mvc.perform(get("/api/posts/{postId}", postId).session(author))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("첫 내용"));

        mvc.perform(patch("/api/posts/{postId}", postId).session(other).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"가로채기\",\"content\":\"수정\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/posts/{postId}", postId).session(other).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/posts/{postId}", postId).session(author).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"수정된 글\",\"content\":\"수정된 내용\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/posts/{postId}", postId).session(author))
                .andExpect(jsonPath("$.title").value("수정된 글"));
        mvc.perform(delete("/api/posts/{postId}", postId).session(author).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/posts/{postId}", postId).session(author))
                .andExpect(status().isNotFound());
    }

    @Test
    void postAcceptsMediaAndSupportsCommentReplies() throws Exception {
        MockHttpSession author = member("media_author", "media-author@example.com");
        MockHttpSession other = member("reply_member", "reply@example.com");
        when(mediaStorage.upload(any())).thenReturn(new StoredMedia(
                "climbers-hub/posts/test-image",
                "https://res.cloudinary.com/demo/image/upload/test-image.png",
                com.climbers.hub.post.domain.MediaType.IMAGE,
                "wall.png",
                "image/png",
                4L));
        MockMultipartFile postPart = new MockMultipartFile("post", "", MediaType.APPLICATION_JSON_VALUE,
                "{\"title\":\"미디어 게시글\",\"content\":\"완등 영상\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        MockMultipartFile image = new MockMultipartFile("files", "wall.png", "image/png", new byte[]{1, 2, 3, 4});
        MvcResult created = mvc.perform(multipart("/api/posts").file(postPart).file(image)
                        .session(author).with(csrf()))
                .andExpect(status().isCreated()).andReturn();
        long postId = Long.parseLong(created.getResponse().getContentAsString());
        mvc.perform(get("/api/posts/{postId}", postId).session(author))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.media[0].mediaType").value("IMAGE"))
                .andExpect(jsonPath("$.media[0].originalName").value("wall.png"));

        MvcResult root = mvc.perform(post("/api/posts/{postId}/comments", postId).session(author).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"첫 댓글\"}"))
                .andExpect(status().isCreated()).andReturn();
        long rootId = Long.parseLong(root.getResponse().getContentAsString());
        MvcResult reply = mvc.perform(post("/api/posts/{postId}/comments", postId).session(other).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"첫 답글\",\"parentCommentId\":" + rootId + "}"))
                .andExpect(status().isCreated()).andReturn();
        long replyId = Long.parseLong(reply.getResponse().getContentAsString());
        mvc.perform(post("/api/posts/{postId}/comments", postId).session(author).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"깊이 초과\",\"parentCommentId\":" + replyId + "}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/posts/{postId}/comments", postId).session(author))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("첫 댓글"))
                .andExpect(jsonPath("$[0].replies[0].content").value("첫 답글"));
        mvc.perform(delete("/api/comments/{commentId}", rootId).session(other).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/comments/{commentId}", rootId).session(author).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/posts/{postId}/comments", postId).session(author))
                .andExpect(jsonPath("$").isEmpty());
    }
}

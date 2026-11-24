package com.climbers.hub.post.controller;

import com.climbers.hub.auth.MemberPrincipal;
import com.climbers.hub.post.dto.PostDto;
import com.climbers.hub.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Post API", description = "전체 자유게시판과 암장별 게시글 API")
@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @Operation(summary = "전체 자유게시판 목록")
    @GetMapping("/api/posts")
    public List<PostDto.PostResponse> getGlobalPosts() {
        return postService.getAllGlobalPosts();
    }

    @Operation(summary = "전체 자유게시판 글 상세")
    @GetMapping("/api/posts/{postId}")
    public PostDto.PostResponse getGlobalPost(@PathVariable Long postId) {
        return postService.getGlobalPost(postId);
    }

    @Operation(summary = "전체 자유게시판 글 작성")
    @PostMapping(value = "/api/posts", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> createGlobalPost(Authentication authentication,
                                                 @Valid @RequestBody PostDto.PostCreateRequest request) {
        Long postId = postService.createGlobalPost(request, requirePrincipal(authentication).getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED).body(postId);
    }

    @Operation(summary = "미디어를 포함한 전체 자유게시판 글 작성")
    @PostMapping(value = "/api/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createGlobalPostWithMedia(Authentication authentication,
                                                          @Valid @RequestPart("post") PostDto.PostCreateRequest request,
                                                          @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        Long postId = postService.createGlobalPost(request, requirePrincipal(authentication).getMemberId(), files);
        return ResponseEntity.status(HttpStatus.CREATED).body(postId);
    }

    @Operation(summary = "게시글 수정")
    @PatchMapping({"/api/posts/{postId}", "/post/{postId}"})
    public ResponseEntity<Void> updatePost(Authentication authentication, @PathVariable Long postId,
                                           @Valid @RequestBody PostDto.PostUpdateRequest request) {
        postService.updatePost(postId, requirePrincipal(authentication).getMemberId(), request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping({"/api/posts/{postId}", "/post/{postId}"})
    public ResponseEntity<Void> deletePost(Authentication authentication, @PathVariable Long postId) {
        postService.deletePost(requirePrincipal(authentication).getMemberId(), postId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "특정 암장 게시글 생성")
    @PostMapping("/gyms/{gymId}/post")
    public ResponseEntity<Long> createGymPost(Authentication authentication, @PathVariable Long gymId,
                                              @Valid @RequestBody PostDto.PostCreateRequest request) {
        Long postId = postService.createGymPost(gymId, request, requirePrincipal(authentication).getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED).body(postId);
    }

    @Operation(summary = "특정 암장 게시글 조회")
    @GetMapping("/gyms/{gymId}/post/{postId}")
    public PostDto.PostResponse getGymPost(@PathVariable Long gymId, @PathVariable Long postId) {
        return postService.getPostByGym(gymId, postId);
    }

    @Operation(summary = "특정 암장 모든 게시글 조회")
    @GetMapping("/gyms/{gymId}/posts")
    public List<PostDto.PostResponse> getGymPosts(@PathVariable Long gymId) {
        return postService.getAllPostByGym(gymId);
    }

    private MemberPrincipal requirePrincipal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof MemberPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "다시 로그인해주세요.");
        }
        return principal;
    }
}

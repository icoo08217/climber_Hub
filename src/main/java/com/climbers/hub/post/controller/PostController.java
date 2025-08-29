package com.climbers.hub.post.controller;

import com.climbers.hub.gym.domain.Gym;
import com.climbers.hub.gym.dto.GymDto;
import com.climbers.hub.gym.repository.GymRepository;
import com.climbers.hub.gym.service.GymService;
import com.climbers.hub.member.repository.MemberRepository;
import com.climbers.hub.post.dto.PostDto;
import com.climbers.hub.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Post API" , description = "암장 공지사항, 전체 공지사항 등 게시글 CRUD")
@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final GymRepository gymRepository;
    private final MemberRepository memberRepository;

    // 특정 암장 게시글 생성
    @Operation(summary = "특정 암장 게시글 생성")
    @PostMapping("/gyms/{gymId}/post")
    public ResponseEntity<Long> createGymPost(@PathVariable Long gymId, @RequestBody PostDto.PostCreateRequest request) {
        // TODO: 로그인된 사용자 정보 가져오기
        String memberEmail = "testtest@naver.com";
        Long postId = postService.createGymPost(gymId, request, memberEmail);
        return ResponseEntity.ok(postId);
    }

    // 특정 암장 게시글 조회
    @Operation(summary = "특정 암장 게시글 조회")
    @GetMapping("/gyms/{gymId}/post/{postId}")
    public ResponseEntity<PostDto.PostResponse> getGymPost(@PathVariable Long gymId, @PathVariable Long postId) {
        PostDto.PostResponse post = postService.getPostByGym(gymId, postId);
        return ResponseEntity.ok(post);
    }

    @Operation(summary = "특정 암장 모든 게시글 조회")
    @GetMapping("/gyms/{gymId}/posts")
    public ResponseEntity<List<PostDto.PostResponse>> getGymAllPost(@PathVariable Long gymId) {
        List<PostDto.PostResponse> postList = postService.getAllPostByGym(gymId);
        return ResponseEntity.ok(postList);
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/post/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        // TODO: 로그인 기능 구현 후 실제 사용자 정보로 교체하기
        String memberEmail = "testtest@naver.com";
        postService.deletePost(memberEmail, postId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "게시글 수정")
    @PatchMapping("/post/{postId}")
    public ResponseEntity<Void> updatePost(@PathVariable Long postId, @RequestBody PostDto.PostUpdateRequest request) {
        // TODO: 로그인 기능 구현 후 실제 사용자 정보로 교체
        String memberEmail = "testtest@naver.com";
        postService.updatePost(postId, memberEmail, request);
        return ResponseEntity.ok().build();
    }

    // 전체 게시글 목록 조회 API 개발
    
}

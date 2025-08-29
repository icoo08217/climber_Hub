package com.climbers.hub.post.service;

import com.climbers.hub.gym.domain.Gym;
import com.climbers.hub.gym.repository.GymRepository;
import com.climbers.hub.member.domain.Member;
import com.climbers.hub.member.repository.MemberRepository;
import com.climbers.hub.post.domain.Post;
import com.climbers.hub.post.dto.PostDto;
import com.climbers.hub.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final GymRepository gymRepository;

    // Post Create
    @Transactional
    public Long createGymPost(Long gymId, PostDto.PostCreateRequest request, String memberEmail) {
        Gym gym = gymRepository.findById(gymId).orElseThrow(() -> new IllegalArgumentException("Not Found Gym"));
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(() -> new IllegalArgumentException("Not Found Member"));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .postType(request.getPostType())
                .member(member)
                .gym(gym)
                .build();

        Post newPost = postRepository.save(post);
        log.info("{} Gym , New Post Create : {}" , gym.getName(), newPost.getPostId());
        return newPost.getPostId();
    }

    // getPostByGym
    @Transactional
    public PostDto.PostResponse getPostByGym(Long gymId, Long postId) {
        log.info("gym ID : {}, post ID : {}, Get PostByGym", gymId , postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post Not Found"));
        // 찾은 게시물이 gym 소속인지 ?
        if(post.getGym() == null || !post.getGym().getGymId().equals(gymId)) {
            throw new IllegalArgumentException("Post with id " + postId + " does not belong to gym with id " + gymId);
        }
        return new PostDto.PostResponse(post);
    }

    // 특정 암장의 모든 게시글 조회
    @Transactional(readOnly = true)
    public List<PostDto.PostResponse> getAllPostByGym(Long gymId) {
        log.info("gym ID : {}, Get All PostPostByGym", gymId);
        List<Post> postList = postRepository.findByGym_GymId(gymId);

        List<PostDto.PostResponse> posts = postList.stream()
                .map(PostDto.PostResponse::new)
                .toList();
        return posts;
    }

    @Transactional
    public void updatePost(Long postId, String memberEmail, PostDto.PostUpdateRequest request) {
        log.info("Post Update Start, POST ID : {}" , postId);

        // 수정할 Post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post Not Found"));

        // 현재 로그인한 사용자가 게시글의 작성자인지 확인
        if (!post.getMember().getEmail().equals(memberEmail)) {
            throw new SecurityException("Permission Denied");
        }

        // Update
        post.update(request.getTitle(), request.getContent());
        log.info("Post Update Success");
    }

    public void deletePost(String memberEmail, Long postId) {
        log.info("member : {} , post ID : {} Post Delete Start" , memberEmail, postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post Not Found"));

        // 현재 로그인한 사용자가 게시글의 작성자인지 ? ++ 현재 로그인한 사용자가 전체 ADMIN 인지 확인하기
        if (!post.getMember().getEmail().equals(memberEmail)) {
            // 작성자가 아니라면 예외를 발생시켜 삭제 방지
            throw new SecurityException("Permission Denied to delete this post");
        }

        // 권한 확인 체크 후 게시글 삭제
        postRepository.delete(post);
        log.info("Post Delete Success");
    }
}

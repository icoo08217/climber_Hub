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
}

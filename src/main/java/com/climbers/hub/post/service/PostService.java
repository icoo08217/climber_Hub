package com.climbers.hub.post.service;

import com.climbers.hub.gym.domain.Gym;
import com.climbers.hub.gym.repository.GymRepository;
import com.climbers.hub.media.service.MediaStorage;
import com.climbers.hub.media.service.StoredMedia;
import com.climbers.hub.member.domain.Member;
import com.climbers.hub.member.repository.MemberRepository;
import com.climbers.hub.post.domain.Post;
import com.climbers.hub.post.dto.PostDto;
import com.climbers.hub.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final GymRepository gymRepository;
    private final MediaStorage mediaStorage;

    // Post Create
    @Transactional
    public Long createGymPost(Long gymId, PostDto.PostCreateRequest request, Long memberId) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "암장을 찾을 수 없습니다."));
        Member member = requireMember(memberId);

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .postType(com.climbers.hub.post.constant.PostType.GYM_POST)
                .member(member)
                .gym(gym)
                .build();

        Post newPost = postRepository.save(post);
        log.info("{} Gym , New Post Create : {}" , gym.getName(), newPost.getPostId());
        return newPost.getPostId();
    }

    @Transactional
    public Long createGlobalPost(PostDto.PostCreateRequest request, Long memberId) {
        return createGlobalPost(request, memberId, List.of());
    }

    @Transactional
    public Long createGlobalPost(PostDto.PostCreateRequest request, Long memberId, List<MultipartFile> files) {
        Member member = requireMember(memberId);
        List<MultipartFile> attachments = files == null ? List.of() : files;
        validateAttachments(attachments);
        List<StoredMedia> uploaded = uploadAll(attachments);
        registerRollbackCleanup(uploaded);
        Post post = Post.builder()
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .postType(com.climbers.hub.post.constant.PostType.GLOBAL_POST)
                .member(member)
                .build();
        for (int index = 0; index < uploaded.size(); index++) {
            post.addMedia(uploaded.get(index), index);
        }
        return postRepository.saveAndFlush(post).getPostId();
    }

    @Transactional(readOnly = true)
    public List<PostDto.PostResponse> getAllGlobalPosts() {
        return postRepository.findByGymIsNullOrderByCreatedAtDesc().stream()
                .map(PostDto.PostResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public PostDto.PostResponse getGlobalPost(Long postId) {
        Post post = requirePost(postId);
        if (post.getGym() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
        return new PostDto.PostResponse(post);
    }

    // getPostByGym
    @Transactional
    public PostDto.PostResponse getPostByGym(Long gymId, Long postId) {
        log.info("gym ID : {}, post ID : {}, Get PostByGym", gymId , postId);
        Post post = requirePost(postId);
        // 찾은 게시물이 gym 소속인지 ?
        if(post.getGym() == null || !post.getGym().getGymId().equals(gymId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 암장의 게시글을 찾을 수 없습니다.");
        }
        return new PostDto.PostResponse(post);
    }

    // 특정 암장의 모든 게시글 조회
    @Transactional(readOnly = true)
    public List<PostDto.PostResponse> getAllPostByGym(Long gymId) {
        log.info("gym ID : {}, Get All PostPostByGym", gymId);
        List<Post> postList = postRepository.findByGym_GymIdOrderByCreatedAtDesc(gymId);

        List<PostDto.PostResponse> posts = postList.stream()
                .map(PostDto.PostResponse::new)
                .toList();
        return posts;
    }

    @Transactional
    public void updatePost(Long postId, Long memberId, PostDto.PostUpdateRequest request) {
        log.info("Post Update Start, POST ID : {}" , postId);

        // 수정할 Post
        Post post = requirePost(postId);

        // 현재 로그인한 사용자가 게시글의 작성자인지 확인
        requireAuthor(post, memberId);

        // Update
        post.update(request.getTitle().trim(), request.getContent().trim());
        log.info("Post Update Success");
    }

    @Transactional
    public void deletePost(Long memberId, Long postId) {
        log.info("member : {} , post ID : {} Post Delete Start" , memberId, postId);
        Post post = requirePost(postId);

        // 현재 로그인한 사용자가 게시글의 작성자인지 ? ++ 현재 로그인한 사용자가 전체 ADMIN 인지 확인하기
        requireAuthor(post, memberId);

        // 권한 확인 체크 후 게시글 삭제
        List<StoredMedia> storedMedia = post.getMedia().stream()
                .map(media -> new StoredMedia(media.getPublicId(), media.getMediaUrl(), media.getMediaType(),
                        media.getOriginalName(), media.getContentType(), media.getFileSize()))
                .toList();
        postRepository.delete(post);
        registerAfterCommitCleanup(storedMedia);
        log.info("Post Delete Success");
    }

    private void validateAttachments(List<MultipartFile> files) {
        if (files.size() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "첨부파일은 게시글당 최대 5개까지 가능합니다.");
        }
        long totalBytes = 0;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "빈 파일은 첨부할 수 없습니다.");
            }
            var mediaType = com.climbers.hub.post.domain.MediaType.fromContentType(file.getContentType());
            if (file.getSize() > mediaType.getMaxBytes()) {
                String limit = mediaType == com.climbers.hub.post.domain.MediaType.IMAGE ? "10MB" : "50MB";
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mediaType.name() + " 파일은 " + limit + " 이하여야 합니다.");
            }
            totalBytes += file.getSize();
        }
        if (totalBytes > 100L * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "첨부파일 전체 용량은 100MB 이하여야 합니다.");
        }
    }

    private List<StoredMedia> uploadAll(List<MultipartFile> files) {
        List<StoredMedia> uploaded = new java.util.ArrayList<>();
        try {
            for (MultipartFile file : files) uploaded.add(mediaStorage.upload(file));
            return uploaded;
        } catch (RuntimeException exception) {
            cleanup(uploaded);
            throw exception;
        }
    }

    private void registerRollbackCleanup(List<StoredMedia> uploaded) {
        if (uploaded.isEmpty()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) cleanup(uploaded);
            }
        });
    }

    private void registerAfterCommitCleanup(List<StoredMedia> storedMedia) {
        if (storedMedia.isEmpty()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cleanup(storedMedia);
            }
        });
    }

    private void cleanup(List<StoredMedia> storedMedia) {
        for (StoredMedia media : storedMedia) {
            try {
                mediaStorage.delete(media.publicId(), media.mediaType());
            } catch (RuntimeException exception) {
                log.warn("Cloud media cleanup failed. publicId={}", media.publicId(), exception);
            }
        }
    }

    private Member requireMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "다시 로그인해주세요."));
    }

    private Post requirePost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
    }

    private void requireAuthor(Post post, Long memberId) {
        if (post.getMember() == null || !post.getMember().getMemberId().equals(memberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글 작성자만 변경할 수 있습니다.");
        }
    }
}

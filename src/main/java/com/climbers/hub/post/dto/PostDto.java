package com.climbers.hub.post.dto;

import com.climbers.hub.gym.domain.Gym;
import com.climbers.hub.member.domain.Member;
import com.climbers.hub.post.constant.PostType;
import com.climbers.hub.post.domain.Post;
import com.climbers.hub.post.domain.PostMedia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class PostDto {

    @Getter
    public static class PostCreateRequest {
        @NotBlank
        @Size(max = 255)
        private String title;

        @NotBlank
        @Size(max = 10000)
        private String content;

        private PostType postType;
    }

    @Getter
    public static class PostUpdateRequest {
        @NotBlank
        @Size(max = 255)
        private String title;

        @NotBlank
        @Size(max = 10000)
        private String content;
    }

    @Getter
    public static class PostResponse {
        private final Long postId;
        private final String title;
        private final String content;
        private final PostType postType;
        private final Long memberId;
        private final String memberName;
        private final Long gymId;
        private final String gymName;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;
        private final List<PostMediaResponse> media;

        public PostResponse(Post post) {
            this.postId = post.getPostId();
            this.title = post.getTitle();
            this.content = post.getContent();
            this.postType = post.getPostType();
            this.memberId = post.getMember().getMemberId();
            this.memberName = post.getMember().getName();
            this.gymId = (post.getGym() != null) ? post.getGym().getGymId() : null;
            this.gymName = (post.getGym() != null) ? post.getGym().getName() : null;
            this.createdAt = post.getCreatedAt();
            this.updatedAt = post.getUpdatedAt();
            this.media = post.getMedia().stream().map(PostMediaResponse::new).toList();
        }
    }

    @Getter
    public static class PostMediaResponse {
        private final Long mediaId;
        private final com.climbers.hub.post.domain.MediaType mediaType;
        private final String mediaUrl;
        private final String originalName;
        private final String contentType;
        private final Long fileSize;
        private final Integer displayOrder;

        public PostMediaResponse(PostMedia media) {
            this.mediaId = media.getMediaId();
            this.mediaType = media.getMediaType();
            this.mediaUrl = media.getMediaUrl();
            this.originalName = media.getOriginalName();
            this.contentType = media.getContentType();
            this.fileSize = media.getFileSize();
            this.displayOrder = media.getDisplayOrder();
        }
    }
}

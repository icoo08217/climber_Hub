package com.climbers.hub.post.dto;

import com.climbers.hub.gym.domain.Gym;
import com.climbers.hub.member.domain.Member;
import com.climbers.hub.post.constant.PostType;
import com.climbers.hub.post.domain.Post;
import lombok.Getter;

public class PostDto {

    @Getter
    public static class PostCreateRequest {
        private String title;
        private String content;
        private PostType postType;
    }

    @Getter
    public static class PostUpdateRequest {
        private String title;
        private String content;
    }

    @Getter
    public static class PostResponse {
        private final Long postId;
        private final String title;
        private final String content;
        private final String memberName;
        private final Long gymId;
        private final String gymName;

        public PostResponse(Post post) {
            this.postId = post.getPostId();
            this.title = post.getTitle();
            this.content = post.getContent();
            this.memberName = post.getMember().getName();
            this.gymId = (post.getGym() != null) ? post.getGym().getGymId() : null;
            this.gymName = (post.getGym() != null) ? post.getGym().getName() : null;
        }
    }
}

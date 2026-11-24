package com.climbers.hub.post.dto;

import com.climbers.hub.post.domain.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class CommentDto {
    private CommentDto() {
    }

    public record CommentCreateRequest(
            @NotBlank @Size(max = 1000) String content,
            Long parentCommentId) {
    }

    public record CommentResponse(
            Long commentId,
            Long memberId,
            String memberName,
            String content,
            LocalDateTime createdAt,
            List<CommentResponse> replies) {
        public static CommentResponse from(Comment comment, List<CommentResponse> replies) {
            return new CommentResponse(
                    comment.getCommentId(),
                    comment.getMember().getMemberId(),
                    comment.getMember().getName(),
                    comment.getContent(),
                    comment.getCreatedAt(),
                    replies);
        }
    }
}

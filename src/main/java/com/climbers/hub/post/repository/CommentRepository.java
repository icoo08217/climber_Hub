package com.climbers.hub.post.repository;

import com.climbers.hub.post.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost_PostIdOrderByCreatedAtAsc(Long postId);

    List<Comment> findByParent_CommentId(Long parentCommentId);
}

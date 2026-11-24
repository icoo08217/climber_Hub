package com.climbers.hub.post.controller;

import com.climbers.hub.auth.MemberPrincipal;
import com.climbers.hub.post.dto.CommentDto;
import com.climbers.hub.post.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/api/posts/{postId}/comments")
    public List<CommentDto.CommentResponse> getComments(@PathVariable Long postId) {
        return commentService.getAll(postId);
    }

    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<Long> createComment(Authentication authentication, @PathVariable Long postId,
                                              @Valid @RequestBody CommentDto.CommentCreateRequest request) {
        Long commentId = commentService.create(postId, requirePrincipal(authentication).getMemberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentId);
    }

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(Authentication authentication, @PathVariable Long commentId) {
        commentService.delete(commentId, requirePrincipal(authentication).getMemberId());
        return ResponseEntity.noContent().build();
    }

    private MemberPrincipal requirePrincipal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof MemberPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "다시 로그인해주세요.");
        }
        return principal;
    }
}

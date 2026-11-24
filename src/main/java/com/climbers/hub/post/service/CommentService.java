package com.climbers.hub.post.service;

import com.climbers.hub.member.domain.Member;
import com.climbers.hub.member.repository.MemberRepository;
import com.climbers.hub.post.domain.Comment;
import com.climbers.hub.post.domain.Post;
import com.climbers.hub.post.dto.CommentDto;
import com.climbers.hub.post.repository.CommentRepository;
import com.climbers.hub.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long create(Long postId, Long memberId, CommentDto.CommentCreateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "다시 로그인해주세요."));
        Comment parent = null;
        if (request.parentCommentId() != null) {
            parent = commentRepository.findById(request.parentCommentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "부모 댓글을 찾을 수 없습니다."));
            if (!parent.getPost().getPostId().equals(postId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "같은 게시글의 댓글에만 답글을 작성할 수 있습니다.");
            }
            if (parent.getParent() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대댓글에는 추가 답글을 작성할 수 없습니다.");
            }
        }
        Comment comment = new Comment(post, member, parent, request.content().trim());
        return commentRepository.save(comment).getCommentId();
    }

    @Transactional(readOnly = true)
    public List<CommentDto.CommentResponse> getAll(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
        List<Comment> comments = commentRepository.findByPost_PostIdOrderByCreatedAtAsc(postId);
        Map<Long, List<CommentDto.CommentResponse>> replies = new LinkedHashMap<>();
        List<Comment> roots = new ArrayList<>();
        for (Comment comment : comments) {
            if (comment.getParent() == null) roots.add(comment);
            else replies.computeIfAbsent(comment.getParent().getCommentId(), ignored -> new ArrayList<>())
                    .add(CommentDto.CommentResponse.from(comment, List.of()));
        }
        return roots.stream()
                .map(comment -> CommentDto.CommentResponse.from(comment,
                        replies.getOrDefault(comment.getCommentId(), List.of())))
                .toList();
    }

    @Transactional
    public void delete(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));
        if (!comment.getMember().getMemberId().equals(memberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글 작성자만 삭제할 수 있습니다.");
        }
        List<Comment> replies = commentRepository.findByParent_CommentId(commentId);
        if (!replies.isEmpty()) {
            commentRepository.deleteAll(replies);
            commentRepository.flush();
        }
        commentRepository.delete(comment);
    }
}

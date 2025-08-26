package com.climbers.hub.post.domain;

import com.climbers.hub.domain.BaseEntity;
import com.climbers.hub.gym.domain.Gym;
import com.climbers.hub.member.domain.Member;
import com.climbers.hub.post.constant.PostType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Getter
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 저장
    @Column(nullable = false)
    private PostType postType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id", nullable = true)
    private Gym gym;

    @Builder
    public Post(String title, String content, PostType postType, Member member, Gym gym) {
        this.title = title;
        this.content = content;
        this.postType = postType;
        this.member = member;
        this.gym = gym;
    }

    // 수정용 메서드
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}

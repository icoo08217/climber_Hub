package com.climbers.hub.post.domain;

import com.climbers.hub.domain.BaseEntity;
import com.climbers.hub.media.service.StoredMedia;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(name = "post_media", uniqueConstraints = {
        @UniqueConstraint(name = "uk_post_media_public_id", columnNames = "public_id"),
        @UniqueConstraint(name = "uk_post_media_order", columnNames = {"post_id", "display_order"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostMedia extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mediaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MediaType mediaType;

    @Column(nullable = false, length = 1000)
    private String mediaUrl;

    @Column(nullable = false, length = 255)
    private String publicId;

    @Column(nullable = false, length = 255)
    private String originalName;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private Integer displayOrder;

    public PostMedia(Post post, StoredMedia stored, int displayOrder) {
        this.post = post;
        this.mediaType = stored.mediaType();
        this.mediaUrl = stored.secureUrl();
        this.publicId = stored.publicId();
        this.originalName = stored.originalName();
        this.contentType = stored.contentType();
        this.fileSize = stored.fileSize();
        this.displayOrder = displayOrder;
    }
}

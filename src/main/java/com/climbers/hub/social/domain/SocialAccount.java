package com.climbers.hub.social.domain;

import com.climbers.hub.domain.BaseEntity;
import com.climbers.hub.member.domain.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "social_accounts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_social_provider_user", columnNames = {"provider", "provider_user_id"}),
        @UniqueConstraint(name = "uk_social_member_provider", columnNames = {"member_id", "provider"})})
public class SocialAccount extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long socialAccountId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_social_member"))
    private Member member;
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(nullable = false, columnDefinition = "varchar(20)", length = 20)
    private SocialProvider provider;
    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    public SocialAccount(Member member, SocialProvider provider, String providerUserId) {
        this.member = member;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }
}

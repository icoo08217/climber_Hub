package com.climbers.hub.gym.domain;

import com.climbers.hub.domain.BaseEntity;
import com.climbers.hub.gym.constant.GymStaffRole;
import com.climbers.hub.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "gym_staff", uniqueConstraints = {
        @UniqueConstraint(name = "uk_gym_staff_member_gym", columnNames = {"member_id", "gym_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GymStaff extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gymStaffId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym;

    @Enumerated(EnumType.STRING)
    @Column(name = "gym_role", nullable = false, length = 20)
    private GymStaffRole gymRole;

    private GymStaff(Member member, Gym gym, GymStaffRole gymRole) {
        this.member = member;
        this.gym = gym;
        this.gymRole = gymRole;
    }

    public static GymStaff owner(Member member, Gym gym) {
        return new GymStaff(member, gym, GymStaffRole.OWNER);
    }
}

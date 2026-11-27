package com.climbers.hub.gym.dto;

import com.climbers.hub.gym.domain.Gym;
import com.climbers.hub.gym.domain.GymStaff;
import com.climbers.hub.member.domain.Member;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public final class GymOwnerAdminDto {
    private GymOwnerAdminDto() {
    }

    public record AssignOwnerRequest(@NotNull Long memberId) {
    }

    public record ReplaceOwnerRequest(@NotNull Long newMemberId) {
    }

    public record MemberSummary(Long memberId, String loginId, String name, String email, String role) {
        public static MemberSummary from(Member member) {
            return new MemberSummary(member.getMemberId(), member.getLoginId(), member.getName(),
                    member.getEmail(), member.getRole().name());
        }
    }

    public record OwnerSummary(Long memberId, String loginId, String name, String email) {
        public static OwnerSummary from(GymStaff staff) {
            Member member = staff.getMember();
            return new OwnerSummary(member.getMemberId(), member.getLoginId(), member.getName(), member.getEmail());
        }
    }

    public record GymSummary(Long gymId, String name, String address, String verificationStatus,
                             boolean active, List<OwnerSummary> owners) {
        public static GymSummary from(Gym gym, List<OwnerSummary> owners) {
            return new GymSummary(gym.getGymId(), gym.getName(), gym.getAddress(),
                    gym.getVerificationStatus().name(), gym.isActive(), owners);
        }
    }

    public record ManagementResponse(List<MemberSummary> members, List<GymSummary> gyms) {
    }
}

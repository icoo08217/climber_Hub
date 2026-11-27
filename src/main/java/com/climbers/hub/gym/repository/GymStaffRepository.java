package com.climbers.hub.gym.repository;

import com.climbers.hub.gym.constant.GymStaffRole;
import com.climbers.hub.gym.domain.GymStaff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GymStaffRepository extends JpaRepository<GymStaff, Long> {
    boolean existsByMember_MemberIdAndGym_GymIdAndGymRole(Long memberId, Long gymId, GymStaffRole gymRole);

    Optional<GymStaff> findByMember_MemberIdAndGym_GymIdAndGymRole(
            Long memberId, Long gymId, GymStaffRole gymRole);
}

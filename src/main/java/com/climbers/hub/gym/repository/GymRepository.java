package com.climbers.hub.gym.repository;

import com.climbers.hub.gym.domain.Gym;
import org.springframework.data.jpa.repository.JpaRepository;
import com.climbers.hub.gym.constant.GymVerificationStatus;
import java.util.List;
import java.util.Optional;

public interface GymRepository extends JpaRepository<Gym, Long> {
    List<Gym> findByVerificationStatusNotOrderByNameAsc(GymVerificationStatus status);
    Optional<Gym> findByGymIdAndVerificationStatusAndActiveTrue(Long gymId, GymVerificationStatus status);
}

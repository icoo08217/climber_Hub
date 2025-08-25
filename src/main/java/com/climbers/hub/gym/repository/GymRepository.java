package com.climbers.hub.gym.repository;

import com.climbers.hub.gym.domain.Gym;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymRepository extends JpaRepository<Gym, Long> {
}

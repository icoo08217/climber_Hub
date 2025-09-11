package com.climbers.hub.timeslot.repository;

import com.climbers.hub.timeslot.domain.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
}

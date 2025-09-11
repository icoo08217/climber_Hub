package com.climbers.hub.reservation.repository;

import com.climbers.hub.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}

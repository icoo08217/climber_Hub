package com.climbers.hub.reservation.domain;

import com.climbers.hub.domain.BaseEntity;
import com.climbers.hub.member.domain.Member;
import com.climbers.hub.reservation.constant.ReservationStatus;
import com.climbers.hub.timeslot.domain.TimeSlot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservation_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timeslot_id" , nullable = false)
    private TimeSlot timeSlot;

    @Column(nullable = false)
    private LocalDate reservationDate; // 예약 날짜

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Builder
    public Reservation(Member member, TimeSlot timeSlot, LocalDate reservationDate, ReservationStatus status) {
        this.member = member;
        this.timeSlot = timeSlot;
        this.reservationDate = reservationDate;
        this.status = status;
    }
}

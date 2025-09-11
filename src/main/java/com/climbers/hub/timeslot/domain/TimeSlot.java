package com.climbers.hub.timeslot.domain;

import com.climbers.hub.domain.BaseEntity;
import com.climbers.hub.gym.domain.Gym;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity @Getter
@Table(name = "timeslot")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeSlot extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long timeslotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id" , nullable = false)
    private Gym gym; // 이 시간대가 속한 클라이밍장

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek; // 예약 가능한 요일

    @Column(nullable = false)
    private LocalTime startTime; // 시작 시간

    @Column(nullable = false)
    private LocalTime endTime; // 종료 시간

    @Column(nullable = false)
    private Integer capacity; // 총 예약 가능인원

    @Builder
    public TimeSlot(Gym gym, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, Integer capacity) {
        this.gym = gym;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
    }
}

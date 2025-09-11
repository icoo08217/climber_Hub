package com.climbers.hub.timeslot.service;

import com.climbers.hub.gym.domain.Gym;
import com.climbers.hub.gym.repository.GymRepository;
import com.climbers.hub.gym.service.GymService;
import com.climbers.hub.timeslot.domain.TimeSlot;
import com.climbers.hub.timeslot.dto.TimeSlotDto;
import com.climbers.hub.timeslot.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final GymService gymService;
    private final GymRepository gymRepository;

    @Transactional
    public Long createTimeSlot(Long gymId, TimeSlotDto request, String userEmail) {
        Gym gym = gymRepository.findById(gymId).orElseThrow(() -> new IllegalArgumentException("Gym not Found"));

        // TODO: 현재 로그인한 사용자(userEmail)가 해당 gym의 소유주인지 확인하는 권한 검사.

        TimeSlot slot = TimeSlot.builder()
                .gym(gym)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .capacity(request.getCapacity())
                .build();

        return timeSlotRepository.save(slot).getTimeslotId();
    }
}

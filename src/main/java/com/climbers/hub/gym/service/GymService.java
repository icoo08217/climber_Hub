package com.climbers.hub.gym.service;

import com.climbers.hub.geocoding.dto.GeocodingDto;
import com.climbers.hub.geocoding.service.GeocodingService;
import com.climbers.hub.gym.Gym;
import com.climbers.hub.gym.dto.GymDto;
import com.climbers.hub.gym.repository.GymRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GymService {

    private final GeocodingService geocodingService;
    private final GymRepository gymRepository;

    @Transactional
    public Long createGym(GymDto.GymCreateRequest request) {
        log.info("New Climbing Gym Create Start : {}", request.getName());

        GeocodingDto.Address coordinates = geocodingService.getCoordinates(request.getAddress());

        Gym newGym = Gym.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .phone(request.getPhone())
                .latitude(Double.parseDouble(coordinates.getY()))
                .longitude(Double.parseDouble(coordinates.getX()))
                .openHours(request.getOpenHours())
                .pricingInfo(request.getPricingInfo())
                .build();

        Gym savedGym = gymRepository.save(newGym);
        log.info("Climbing Gym Create Success : {}", savedGym.getGymId());

        return savedGym.getGymId();
    }

}

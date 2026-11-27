package com.climbers.hub.gym.service;

import com.climbers.hub.geocoding.dto.GeocodingDto;
import com.climbers.hub.geocoding.service.GeocodingService;
import com.climbers.hub.gym.domain.Gym;
import com.climbers.hub.gym.constant.GymVerificationStatus;
import com.climbers.hub.gym.dto.GymDto;
import com.climbers.hub.gym.repository.GymRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GymService {

    private final GeocodingService geocodingService;
    private final GymRepository gymRepository;

    @Transactional(readOnly = true)
    public GymDto.GymDetailResponse getGym(Long gymId) {
        log.info("GymId: {}, Climbing Gym 조회 시작.", gymId);
        Gym findGym = gymRepository.findByGymIdAndVerificationStatusAndActiveTrue(
                        gymId, GymVerificationStatus.VERIFIED)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "암장을 찾을 수 없습니다."));
        return new GymDto.GymDetailResponse(findGym);
    }

    @Transactional(readOnly = true)
    public List<GymDto.GymSimpleResponse> getAllGym() {
        log.info("Climbing Gym 모두 조회 시작.");
        List<GymDto.GymSimpleResponse> gyms = gymRepository
                .findByVerificationStatusNotOrderByNameAsc(GymVerificationStatus.REJECTED).stream()
                .map(GymDto.GymSimpleResponse::new)
                .toList();
        return gyms;
    }

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

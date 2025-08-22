package com.climbers.hub.gym.service;

import com.climbers.hub.geocoding.service.GeocodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GymService {

    private final GeocodingService geocodingService;
    
}

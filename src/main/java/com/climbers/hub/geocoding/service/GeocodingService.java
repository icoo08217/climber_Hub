package com.climbers.hub.geocoding.service;

import com.climbers.hub.geocoding.dto.GeocodingDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class GeocodingService {

    private final WebClient webClient;
    private final String naverClientId;
    private final String naverClientSecret;

    public GeocodingService(@Value("${naver.api.client-id}") String naverClientId,
                            @Value("${naver.api.client-secret}") String naverClientSecret) {
        this.webClient = WebClient.create("https://naveropenapi.apigw.ntruss.com");
        this.naverClientId = naverClientId;
        this.naverClientSecret = naverClientSecret;
    }

    public GeocodingDto.Address getCoordinates(String address) {
        log.info("geocoding request start: {}" , address);

        Mono<GeocodingDto> mono = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/map-geocode/v2/geocode")
                        .queryParam("query" , address)
                        .build())
                .header("X-NCP-APIGW-API-KEY-ID", naverClientId)
                .header("X-NCP-APIGW-API-KEY", naverClientSecret)
                .retrieve()
                .bodyToMono(GeocodingDto.class);

        GeocodingDto response = mono.block();

        if (response == null || response.getAddresses() == null || response.getAddresses().isEmpty()) {
            log.error("주소에 대한 좌표를 찾을 수 없습니다.: {}" , address);
            throw new IllegalArgumentException("Invalid Address provided");
        }

        GeocodingDto.Address coordinates = response.getAddresses().get(0);
        log.info("Geocoding Success: x={} , y={}", coordinates.getX(), coordinates.getY());
        return coordinates;
    }
}

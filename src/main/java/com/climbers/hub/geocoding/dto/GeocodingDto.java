package com.climbers.hub.geocoding.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodingDto {
    private List<Address> addresses;

    @Getter
    @NoArgsConstructor
    public static class Address {
        private String x; // 경도
        private String y; // 위도
    }
}

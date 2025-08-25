package com.climbers.hub.gym.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Getter
@Table(name = "gym")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gym {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gymId;

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    // 위치 정보
    @Column(nullable = false)
    private String address;

    private String phone;

    private Double latitude; // 위도

    private Double longitude; // 경도

    private String openHours;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String pricingInfo;

    @Builder
    public Gym(String name, String description, String address, String phone, Double latitude, Double longitude, String openHours, String pricingInfo) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.openHours = openHours;
        this.pricingInfo = pricingInfo;
    }

}

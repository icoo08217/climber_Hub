package com.climbers.hub.gym.domain;

import com.climbers.hub.domain.BaseEntity;
import com.climbers.hub.gym.constant.GymDataSource;
import com.climbers.hub.gym.constant.GymVerificationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Getter
@Table(name = "gym",
        uniqueConstraints = @UniqueConstraint(name = "uk_gym_external_source_id",
                columnNames = {"external_source", "external_id"}),
        indexes = @Index(name = "idx_gym_verification_active",
                columnList = "verification_status, active"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gym extends BaseEntity {
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

    @Enumerated(EnumType.STRING)
    @Column(name = "external_source", nullable = false, length = 20)
    private GymDataSource externalSource;

    @Column(name = "external_id")
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private GymVerificationStatus verificationStatus;

    @Column(nullable = false)
    private boolean active;

    private java.time.LocalDateTime verifiedAt;

    @Builder
    public Gym(String name, String description, String address, String phone, Double latitude, Double longitude,
               String openHours, String pricingInfo, GymDataSource externalSource, String externalId,
               GymVerificationStatus verificationStatus, Boolean active, java.time.LocalDateTime verifiedAt) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.openHours = openHours;
        this.pricingInfo = pricingInfo;
        this.externalSource = externalSource == null ? GymDataSource.ADMIN : externalSource;
        this.externalId = externalId;
        this.verificationStatus = verificationStatus == null ? GymVerificationStatus.PENDING : verificationStatus;
        this.active = active == null || active;
        this.verifiedAt = verifiedAt;
    }

}

package com.climbers.hub.gym.dto;

import com.climbers.hub.gym.Gym;
import lombok.Getter;

public class GymDto {

    @Getter
    public static class GymCreateRequest {
        private String name;
        private String description;
        private String address;
        private String phone;
        private String openHours;
        private String pricingInfo;
    }

    // 전체 목록 조회용
    @Getter
    public static class GymSimpleResponse {
        private final Long gymId;
        private final String name;
        private final String description;
        private final String address;
        private final String phone;

        public GymSimpleResponse(Gym gym) {
            this.gymId = gym.getGymId();
            this.name = gym.getName();
            this.description = gym.getDescription();
            this.address = gym.getAddress();
            this.phone = gym.getPhone();
        }
    }

    // Gym 상세 조회용
    @Getter
    public static class GymDetailResponse {
        private final Long gymId;
        private final String name;
        private final String description;
        private final String address;
        private final String phone;
        private final Double latitude;
        private final Double longitude;
        private final String openHours;
        private final String pricingInfo;

        public GymDetailResponse(Gym gym) {
            this.gymId = gym.getGymId();
            this.name = gym.getName();
            this.description = gym.getDescription();
            this.address = gym.getAddress();
            this.phone = gym.getPhone();
            this.latitude = gym.getLatitude();
            this.longitude = gym.getLongitude();
            this.openHours = gym.getOpenHours();
            this.pricingInfo = gym.getPricingInfo();
        }
    }
}

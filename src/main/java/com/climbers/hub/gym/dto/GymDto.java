package com.climbers.hub.gym.dto;

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
}

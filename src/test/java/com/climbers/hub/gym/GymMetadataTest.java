package com.climbers.hub.gym;

import com.climbers.hub.gym.constant.GymDataSource;
import com.climbers.hub.gym.constant.GymVerificationStatus;
import com.climbers.hub.gym.domain.Gym;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GymMetadataTest {
    @Test
    void manuallyCreatedGymStartsActiveAndPendingVerification() {
        Gym gym = Gym.builder()
                .name("테스트 암장")
                .address("서울시 테스트구 1")
                .build();

        assertThat(gym.getExternalSource()).isEqualTo(GymDataSource.ADMIN);
        assertThat(gym.getExternalId()).isNull();
        assertThat(gym.getVerificationStatus()).isEqualTo(GymVerificationStatus.PENDING);
        assertThat(gym.isActive()).isTrue();
        assertThat(gym.getVerifiedAt()).isNull();
    }
}

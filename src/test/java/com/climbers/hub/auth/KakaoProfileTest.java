package com.climbers.hub.auth;

import com.climbers.hub.social.security.KakaoProfileClient;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

class KakaoProfileTest {
    private final KakaoProfileClient client = new KakaoProfileClient();
    @Test void absentOptionalFieldsAreAccepted() {
        var identity = client.parse(Map.of("id", 123L));
        assertThat(identity.subject()).isEqualTo("123");
        assertThat(identity.verifiedEmail()).isNull();
        assertThat(identity.nickname()).isNull();
    }
    @Test void onlyValidVerifiedEmailIsUsed() {
        assertThat(client.parse(Map.of("id", 123, "kakao_account", Map.of("email", "a@example.com", "is_email_valid", true))).verifiedEmail()).isNull();
        assertThat(client.parse(Map.of("id", 123, "kakao_account", Map.of("email", "a@example.com", "is_email_valid", true, "is_email_verified", true))).verifiedEmail()).isEqualTo("a@example.com");
    }
    @Test void subjectIsRequired() {
        assertThatThrownBy(() -> client.parse(Map.of())).isInstanceOf(OAuth2AuthenticationException.class);
    }
}

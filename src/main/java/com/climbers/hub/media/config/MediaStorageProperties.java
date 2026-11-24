package com.climbers.hub.media.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.media.cloudinary")
@Getter
@Setter
public class MediaStorageProperties {
    private boolean enabled;
    private String cloudName;
    private String apiKey;
    private String apiSecret;
}

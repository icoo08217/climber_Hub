package com.climbers.hub.media.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
public class CloudinaryConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "app.media.cloudinary", name = "enabled", havingValue = "true")
    public Cloudinary cloudinary(MediaStorageProperties properties) {
        Assert.hasText(properties.getCloudName(), "Cloudinary cloud name is required");
        Assert.hasText(properties.getApiKey(), "Cloudinary API key is required");
        Assert.hasText(properties.getApiSecret(), "Cloudinary API secret is required");
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", properties.getCloudName(),
                "api_key", properties.getApiKey(),
                "api_secret", properties.getApiSecret(),
                "secure", true));
    }
}

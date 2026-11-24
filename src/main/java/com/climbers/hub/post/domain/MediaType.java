package com.climbers.hub.post.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

public enum MediaType {
    IMAGE(Set.of("image/jpeg", "image/png", "image/webp", "image/gif"), 10L * 1024 * 1024),
    VIDEO(Set.of("video/mp4", "video/webm", "video/quicktime"), 50L * 1024 * 1024);

    private final Set<String> contentTypes;
    private final long maxBytes;

    MediaType(Set<String> contentTypes, long maxBytes) {
        this.contentTypes = contentTypes;
        this.maxBytes = maxBytes;
    }

    public long getMaxBytes() {
        return maxBytes;
    }

    public static MediaType fromContentType(String contentType) {
        return java.util.Arrays.stream(values())
                .filter(type -> contentType != null && type.contentTypes.contains(contentType.toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "JPG, PNG, WEBP, GIF 이미지 또는 MP4, WEBM, MOV 동영상만 첨부할 수 있습니다."));
    }
}

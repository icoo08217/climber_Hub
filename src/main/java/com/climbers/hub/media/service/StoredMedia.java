package com.climbers.hub.media.service;

import com.climbers.hub.post.domain.MediaType;

public record StoredMedia(
        String publicId,
        String secureUrl,
        MediaType mediaType,
        String originalName,
        String contentType,
        long fileSize) {
}

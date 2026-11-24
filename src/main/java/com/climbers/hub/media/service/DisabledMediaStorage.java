package com.climbers.hub.media.service;

import com.climbers.hub.post.domain.MediaType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnProperty(prefix = "app.media.cloudinary", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DisabledMediaStorage implements MediaStorage {
    @Override
    public StoredMedia upload(MultipartFile file) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "미디어 저장소가 설정되지 않았습니다.");
    }

    @Override
    public void delete(String publicId, MediaType mediaType) {
        // Nothing is stored while media storage is disabled.
    }
}

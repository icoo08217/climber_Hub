package com.climbers.hub.media.service;

import com.climbers.hub.post.domain.MediaType;
import org.springframework.web.multipart.MultipartFile;

public interface MediaStorage {
    StoredMedia upload(MultipartFile file);

    void delete(String publicId, MediaType mediaType);
}

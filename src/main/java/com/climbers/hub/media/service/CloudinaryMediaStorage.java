package com.climbers.hub.media.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.climbers.hub.post.domain.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.media.cloudinary", name = "enabled", havingValue = "true")
public class CloudinaryMediaStorage implements MediaStorage {
    private final Cloudinary cloudinary;

    @Override
    public StoredMedia upload(MultipartFile file) {
        MediaType mediaType = MediaType.fromContentType(file.getContentType());
        String resourceType = mediaType == MediaType.VIDEO ? "video" : "image";
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", resourceType,
                    "asset_folder", "climbers-hub/posts",
                    "use_filename", false,
                    "unique_filename", true));
            return new StoredMedia(
                    required(result, "public_id"),
                    required(result, "secure_url"),
                    mediaType,
                    safeOriginalName(file.getOriginalFilename()),
                    file.getContentType(),
                    file.getSize());
        } catch (IOException | RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "미디어 업로드에 실패했습니다.", exception);
        }
    }

    @Override
    public void delete(String publicId, MediaType mediaType) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "resource_type", mediaType == MediaType.VIDEO ? "video" : "image",
                    "invalidate", true));
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Cloudinary media deletion failed", exception);
        }
    }

    private String required(Map<?, ?> result, String key) {
        Object value = result.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalStateException("Cloudinary response is missing " + key);
        }
        return value.toString();
    }

    private String safeOriginalName(String originalName) {
        if (originalName == null || originalName.isBlank()) return "media";
        String normalized = originalName.replace('\\', '/');
        String fileName = normalized.substring(normalized.lastIndexOf('/') + 1);
        return fileName.length() > 255 ? fileName.substring(fileName.length() - 255) : fileName;
    }
}

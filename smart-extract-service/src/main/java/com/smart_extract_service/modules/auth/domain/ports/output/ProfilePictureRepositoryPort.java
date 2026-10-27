package com.smart_extract_service.modules.auth.domain.ports.output;

import com.smart_extract_service.modules.auth.domain.model.UserProfilePicture;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ProfilePictureRepositoryPort {
    Optional<UserProfilePicture> findByUserId(UUID userId);
    UserProfilePicture save(UserProfilePicture profilePicture);
    void deleteByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
    Optional<byte[]> findThumbnailByUserId(UUID userId);
    Optional<PictureMetadata> findMetadataByUserId(UUID userId);
    Set<UUID> findUsersWithPhotos(Set<UUID> userIds);

    record PictureMetadata(String contentType, Integer fileSize, Integer thumbnailSize, LocalDateTime updatedAt) {}
}
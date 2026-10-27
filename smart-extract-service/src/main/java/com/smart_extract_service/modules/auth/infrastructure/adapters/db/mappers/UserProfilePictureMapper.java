package com.smart_extract_service.modules.auth.infrastructure.adapters.db.mappers;

import com.smart_extract_service.modules.auth.domain.model.UserProfilePicture;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.UserProfilePictureEntity;

public class UserProfilePictureMapper {

    public static UserProfilePicture toDomain(UserProfilePictureEntity entity) {
        if (entity == null) return null;
        return new UserProfilePicture(
                entity.getUserId(),
                entity.getPictureData(),
                entity.getContentType(),
                entity.getFileSize(),
                entity.getFileName(),
                entity.getThumbnailData(),
                entity.getThumbnailSize(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static UserProfilePictureEntity toEntity(UserProfilePicture domain) {
        if (domain == null) return null;
        return UserProfilePictureEntity.builder()
                .userId(domain.getUserId())
                .pictureData(domain.getPictureData())
                .contentType(domain.getContentType())
                .fileSize(domain.getFileSize())
                .fileName(domain.getFileName())
                .thumbnailData(domain.getThumbnailData())
                .thumbnailSize(domain.getThumbnailSize())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
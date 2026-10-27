package com.smart_extract_service.modules.auth.infrastructure.adapters.db;

import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.UserProfilePictureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface JpaProfilePictureRepository extends JpaRepository<UserProfilePictureEntity, UUID> {

    @Query("SELECT p FROM UserProfilePictureEntity p WHERE p.user.id = :userId")
    Optional<UserProfilePictureEntity> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT p.thumbnailData FROM UserProfilePictureEntity p WHERE p.user.id = :userId")
    Optional<byte[]> findThumbnailByUserId(@Param("userId") UUID userId);

    void deleteByUserId(UUID userId);

    @Query("SELECT p.user.id FROM UserProfilePictureEntity p WHERE p.user.id IN :userIds")
    Set<UUID> findUserIdsWithPhotos(@Param("userIds") Set<UUID> userIds);
}
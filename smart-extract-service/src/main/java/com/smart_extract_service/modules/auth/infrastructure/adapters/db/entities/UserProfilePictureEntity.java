package com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profile_pictures")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfilePictureEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "picture_data", nullable = false)
    private byte[] pictureData;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Integer fileSize;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "thumbnail_data")
    private byte[] thumbnailData;

    @Column(name = "thumbnail_size")
    private Integer thumbnailSize;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
package com.smart_extract_service.modules.auth.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserProfilePicture {

    private UUID userId;
    private byte[] pictureData;
    private String contentType;
    private Integer fileSize;
    private String fileName;
    private byte[] thumbnailData;
    private Integer thumbnailSize;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserProfilePicture() {}

    public UserProfilePicture(UUID userId, byte[] pictureData, String contentType,
                              Integer fileSize, String fileName, byte[] thumbnailData,
                              Integer thumbnailSize, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.pictureData = pictureData;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.thumbnailData = thumbnailData;
        this.thumbnailSize = thumbnailSize;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updatePicture(byte[] newPictureData, byte[] newThumbnailData,
                              String contentType, String fileName, Integer fileSize, Integer thumbnailSize) {
        this.pictureData = newPictureData;
        this.thumbnailData = newThumbnailData;
        this.contentType = contentType;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.thumbnailSize = thumbnailSize;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public byte[] getPictureData() { return pictureData; }
    public void setPictureData(byte[] pictureData) { this.pictureData = pictureData; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Integer getFileSize() { return fileSize; }
    public void setFileSize(Integer fileSize) { this.fileSize = fileSize; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public byte[] getThumbnailData() { return thumbnailData; }
    public void setThumbnailData(byte[] thumbnailData) { this.thumbnailData = thumbnailData; }

    public Integer getThumbnailSize() { return thumbnailSize; }
    public void setThumbnailSize(Integer thumbnailSize) { this.thumbnailSize = thumbnailSize; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

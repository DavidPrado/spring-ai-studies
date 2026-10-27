package com.smart_extract_service.modules.auth.application.service;

import com.smart_extract_service.core.exception.BusinessException;
import com.smart_extract_service.modules.auth.domain.model.User;
import com.smart_extract_service.modules.auth.domain.model.UserProfilePicture; // 👈 Import de Domínio
import com.smart_extract_service.modules.auth.domain.ports.output.ProfilePictureRepositoryPort;
import com.smart_extract_service.modules.auth.domain.ports.output.UserRepositoryPort;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@EnableAsync
public class AsyncProfilePictureService {

    private final ProfilePictureRepositoryPort pictureRepository;
    private final UserRepositoryPort userRepository;

    private static final int THUMBNAIL_SIZE = 150;


    public AsyncProfilePictureService(ProfilePictureRepositoryPort pictureRepository, UserRepositoryPort userRepository) {
        this.pictureRepository = pictureRepository;
        this.userRepository = userRepository;
    }

    @Async
    @Transactional
    public CompletableFuture<Void> saveProfilePictureAsync(
            UUID userId, UUID tenantId,
            byte[] originalBytes, String contentType, String fileName) {

        log.info("Processando foto assíncrona para usuário: {}", userId);
        long startTime = System.currentTimeMillis();
        try {
            userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException("auth.user.not_found"));

            byte[] thumbnailBytes = createThumbnail(originalBytes);

            UserProfilePicture picture = pictureRepository.findByUserId(userId)
                    .orElse(new UserProfilePicture());

            picture.setUserId(userId);
            picture.setPictureData(originalBytes);
            picture.setThumbnailData(thumbnailBytes);
            picture.setContentType(contentType);
            picture.setFileSize(originalBytes.length);
            picture.setThumbnailSize(thumbnailBytes.length);
            picture.setFileName(fileName);
            picture.setUpdatedAt(LocalDateTime.now());
            if (picture.getCreatedAt() == null) picture.setCreatedAt(LocalDateTime.now());

            pictureRepository.save(picture);

            log.info("Foto salva em {} ms para usuário: {} ({} bytes / thumb {} bytes)",
                    System.currentTimeMillis() - startTime, userId,
                    originalBytes.length, thumbnailBytes.length);

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Erro ao salvar foto para usuário: {}", userId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private byte[] createThumbnail(byte[] originalBytes) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalBytes));
        if (originalImage == null) {
            throw new IllegalArgumentException("Não foi possível ler a imagem");
        }

        int width  = originalImage.getWidth();
        int height = originalImage.getHeight();

        int targetWidth, targetHeight;
        if (width > height) {
            targetWidth  = THUMBNAIL_SIZE;
            targetHeight = Math.max(1, (THUMBNAIL_SIZE * height) / width);
        } else {
            targetHeight = THUMBNAIL_SIZE;
            targetWidth  = Math.max(1, (THUMBNAIL_SIZE * width) / height);
        }

        BufferedImage thumbnail = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnail.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, targetWidth, targetHeight);
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(thumbnail, "jpg", baos);
        return baos.toByteArray();
    }


    @Transactional
    public byte[] getThumbnail(UUID userId) {
        return pictureRepository.findThumbnailByUserId(userId).orElse(null);
    }

    @Transactional
    public UserProfilePicture getFullPicture(UUID userId) {
        return pictureRepository.findByUserId(userId).orElse(null);
    }

    @Transactional
    public ProfilePictureRepositoryPort.PictureMetadata getPictureMetadata(UUID userId) {
        return pictureRepository.findMetadataByUserId(userId).orElse(null);
    }

    @Transactional
    public boolean hasProfilePicture(UUID userId) {
        return pictureRepository.existsByUserId(userId);
    }

    @Transactional
    public void deleteProfilePicture(UUID userId) {
        pictureRepository.deleteByUserId(userId);
        log.info("Foto removida para usuário: {}", userId);
    }
}
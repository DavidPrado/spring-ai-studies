package com.smart_extract_service.modules.auth.infrastructure.adapters.web;

import com.smart_extract_service.core.context.TenantContext;
import com.smart_extract_service.core.context.UserContext;
import com.smart_extract_service.core.exception.BusinessException;
import com.smart_extract_service.modules.auth.application.service.AsyncProfilePictureService;
import com.smart_extract_service.modules.auth.infrastructure.adapters.web.support.ProfilePictureValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/users")
public class ProfilePictureController {

    private final AsyncProfilePictureService profilePictureService;
    private final ProfilePictureValidator validator;

    public ProfilePictureController(AsyncProfilePictureService profilePictureService,
                                    ProfilePictureValidator validator) {
        this.profilePictureService = profilePictureService;
        this.validator = validator;
    }

    @PostMapping("/{userId}/profile-picture")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(
            @PathVariable UUID userId,
            @RequestParam("file") MultipartFile file) {

        enforceSelfOrAdmin(userId);
        validator.validate(file);

        byte[] originalBytes;
        try {
            originalBytes = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException("upload.read_error", "Falha ao ler o arquivo");
        }

        profilePictureService.saveProfilePictureAsync(
                userId,
                TenantContext.getCurrentTenant(),
                originalBytes,
                file.getContentType(),
                file.getOriginalFilename());

        return ResponseEntity.accepted().body(Map.of(
                "message", "Foto recebida. Processamento em segundo plano.",
                "userId", userId.toString(),
                "status", "processing"
        ));
    }

    @GetMapping(value = "/{userId}/profile-picture/thumbnail", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getThumbnail(@PathVariable UUID userId) {
        byte[] thumbnail = profilePictureService.getThumbnail(userId);
        if (thumbnail == null) {
            return ResponseEntity.ok(getDefaultAvatar());
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(thumbnail);
    }

    @GetMapping(value = "/{userId}/profile-picture/full", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getFullPicture(@PathVariable UUID userId) {
        enforceSelfOrAdmin(userId);
        var picture = profilePictureService.getFullPicture(userId);
        if (picture == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(picture.getContentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)))
                .body(picture.getPictureData());
    }

    @GetMapping("/{userId}/profile-picture/info")
    public ResponseEntity<Map<String, Object>> getPictureInfo(@PathVariable UUID userId) {
        var metadata = profilePictureService.getPictureMetadata(userId);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
                "hasPicture", true,
                "contentType", metadata.contentType(),
                "fileSize", metadata.fileSize(),
                "thumbnailSize", metadata.thumbnailSize(),
                "updatedAt", metadata.updatedAt()
        ));
    }

    @GetMapping(value = "/me/profile-picture/thumbnail", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getMyThumbnail(){
        byte[] thumbnail = profilePictureService.getThumbnail(UserContext.getUserId());
        if (thumbnail == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                .body(thumbnail);
    }

    @DeleteMapping("/{userId}/profile-picture")
    public ResponseEntity<Void> deleteProfilePicture(@PathVariable UUID userId) {
        enforceSelfOrAdmin(userId);
        profilePictureService.deleteProfilePicture(userId);
        return ResponseEntity.noContent().build();
    }

    private void enforceSelfOrAdmin(UUID targetUserId) {
        UUID currentUserId = UserContext.getUserId();
        if (currentUserId.equals(targetUserId)) return;
        if (hasAnyAdminRole()) return;
        throw new BusinessException("upload.forbidden",
                "Apenas o próprio usuário ou um administrador pode alterar esta foto.");
    }

    private boolean hasAnyAdminRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .anyMatch(a -> "ROLE_SYSTEM_ADMIN".equals(a) || "ROLE_ADMIN".equals(a));
    }

    private byte[] getDefaultAvatar() {
        try {
            return getClass().getResourceAsStream("/static/default-avatar.png").readAllBytes();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
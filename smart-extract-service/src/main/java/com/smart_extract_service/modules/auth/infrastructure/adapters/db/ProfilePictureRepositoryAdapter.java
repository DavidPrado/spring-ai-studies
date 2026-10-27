package com.smart_extract_service.modules.auth.infrastructure.adapters.db;

import com.smart_extract_service.modules.auth.domain.model.UserProfilePicture;
import com.smart_extract_service.modules.auth.domain.ports.output.ProfilePictureRepositoryPort;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.UserEntity;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.UserProfilePictureEntity;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.mappers.UserProfilePictureMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class ProfilePictureRepositoryAdapter implements ProfilePictureRepositoryPort {
    private final JpaProfilePictureRepository repository;
    private final EntityManager entityManager;

    public ProfilePictureRepositoryAdapter(JpaProfilePictureRepository repository,
                                           EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<UserProfilePicture> findByUserId(UUID userId) {
        return repository.findById(userId)
                .map(UserProfilePictureMapper::toDomain);
    }

    @Override
    public UserProfilePicture save(UserProfilePicture profilePicture) {
        UserProfilePictureEntity entity = UserProfilePictureMapper.toEntity(profilePicture);

        if (entity.getUser() == null && profilePicture.getUserId() != null) {
            entity.setUser(entityManager.getReference(
                    UserEntity.class, profilePicture.getUserId()));
        }

        if (repository.existsById(entity.getUserId())){
            entity = repository.save(entity);
        }else{
            entityManager.persist(entity);
            entityManager.flush();
        }

        return UserProfilePictureMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public void deleteByUserId(UUID userId) {
        repository.deleteByUserId(userId);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return repository.existsById(userId);
    }

    @Override
    public Optional<byte[]> findThumbnailByUserId(UUID userId) {
        return repository.findThumbnailByUserId(userId);
    }

    @Override
    public Optional<PictureMetadata> findMetadataByUserId(UUID userId) {
        return repository.findByUserId(userId)
                .map(entity -> new PictureMetadata(
                        entity.getContentType(),
                        entity.getFileSize() != null ? entity.getFileSize().intValue() : null,
                        entity.getThumbnailData() != null ? entity.getThumbnailData().length : null, // ou getThumbnailSize() se houver o campo
                        entity.getUpdatedAt()
                ));
    }

    @Override
    public Set<UUID> findUsersWithPhotos(Set<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Set.of();
        }
        return repository.findUserIdsWithPhotos(userIds);
    }
}
package com.smart_extract_service.modules.auth.infrastructure.adapters.db.mappers;

import com.smart_extract_service.modules.auth.domain.model.UserPasswordHistory;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.UserPasswordHistoryEntity;

public class UserPasswordHistoryMapper {

    public static UserPasswordHistory toDomain(UserPasswordHistoryEntity entity) {
        if (entity == null) return null;

        return new UserPasswordHistory(
                entity.getId(),
                entity.getUserId(),
                entity.getPasswordHash(),
                entity.getCreatedAt()
        );
    }

    public static UserPasswordHistoryEntity toEntity(UserPasswordHistory domain) {
        if (domain == null) return null;

        return UserPasswordHistoryEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .passwordHash(domain.getPasswordHash())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
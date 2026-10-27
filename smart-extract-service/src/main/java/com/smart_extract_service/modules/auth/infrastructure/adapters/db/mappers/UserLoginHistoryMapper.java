package com.smart_extract_service.modules.auth.infrastructure.adapters.db.mappers;

import com.smart_extract_service.modules.auth.domain.model.User;
import com.smart_extract_service.modules.auth.domain.model.UserLoginHistory;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.UserEntity;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.UserLoginHistoryEntity;

public class UserLoginHistoryMapper {

    public static UserLoginHistory toDomain(UserLoginHistoryEntity entity) {
        if (entity == null) return null;

        User domainUser = null;
        if (entity.getUser() != null) {
            domainUser = UserMapper.toDomain(entity.getUser()); // Ou instantiate um User passando entity.getUser().getId()
        }

        return UserLoginHistory.builder()
                .id(entity.getId())
                .user(domainUser)
                .loginAt(entity.getLoginAt())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .successful(entity.getSuccessful())
                .failureReason(entity.getFailureReason())
                .build();
    }

    public static UserLoginHistoryEntity toEntity(UserLoginHistory domain) {
        if (domain == null) return null;

        UserEntity userEntity = null;
        if (domain.getUser() != null && domain.getUser().getId() != null) {
            userEntity = new UserEntity();
            userEntity.setId(domain.getUser().getId()); // Apenas o ID é suficiente para a Foreign Key
        }

        return UserLoginHistoryEntity.builder()
                .id(domain.getId())
                .user(userEntity)
                .loginAt(domain.getLoginAt())
                .ipAddress(domain.getIpAddress())
                .userAgent(domain.getUserAgent())
                .successful(domain.isSuccessful())
                .failureReason(domain.getFailureReason())
                .build();
    }
}
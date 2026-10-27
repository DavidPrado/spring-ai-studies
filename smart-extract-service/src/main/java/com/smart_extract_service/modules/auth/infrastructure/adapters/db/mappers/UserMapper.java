package com.smart_extract_service.modules.auth.infrastructure.adapters.db.mappers;

import com.smart_extract_service.modules.auth.domain.model.User;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.TenantEntity;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.UserEntity;

import java.util.stream.Collectors;

public class UserMapper {

    public static User toDomain(UserEntity entity) {
        if (entity == null) return null;

        User user = new User();
        user.setId(entity.getId());
        user.setEmail(entity.getEmail());
        user.setPasswordHash(entity.getPasswordHash());
        user.setFullName(entity.getFullName());
        user.setPhoneNumber(entity.getPhoneNumber());
        user.setPhoneNumber2(entity.getPhoneNumber2());
        user.setDepartment(entity.getDepartment());
        user.setPosition(entity.getPosition());
        user.setCostCenter(entity.getCostCenter());
        user.setEmployeeCode(entity.getEmployeeCode());
        user.setActive(entity.getActive());
        user.setRequirePasswordChange(entity.getRequirePasswordChange());
        user.setExpiresAt(entity.getExpiresAt());
        user.setLastLoginAt(entity.getLastLoginAt());
        user.setLastLoginIp(entity.getLastLoginIp());
        user.setFailedLoginAttempts(entity.getFailedLoginAttempts());
        user.setLockedUntil(entity.getLockedUntil());
        user.setCreatedAt(entity.getCreatedAt());
        user.setUpdatedAt(entity.getUpdatedAt());
        user.setLastPasswordChangeAt(entity.getLastPasswordChangeAt());
        user.setIsTemporary(entity.isTemporary());
        user.setPasswordChangeIntervalDays(entity.getPasswordChangeIntervalDays());

        if (entity.getTenant() != null) {
            user.setTenantId(entity.getTenant().getId());
        }

        if (entity.getRoles() != null) {
            user.setRoles(entity.getRoles().stream()
                    .map(RoleMapper::toDomain)
                    .collect(Collectors.toSet()));
        }

        return user;
    }

    public static UserEntity toEntity(User domain, TenantEntity tenantEntity) {
        if (domain == null) return null;

        UserEntity entity = new UserEntity();
        entity.setId(domain.getId());
        entity.setTenant(tenantEntity);
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setFullName(domain.getFullName());
        entity.setPhoneNumber(domain.getPhoneNumber());
        entity.setPhoneNumber2(domain.getPhoneNumber2());
        entity.setDepartment(domain.getDepartment());
        entity.setPosition(domain.getPosition());
        entity.setCostCenter(domain.getCostCenter());
        entity.setEmployeeCode(domain.getEmployeeCode());
        entity.setActive(domain.getActive() != null ? domain.getActive() : true);
        entity.setRequirePasswordChange(domain.getRequirePasswordChange() != null ? domain.getRequirePasswordChange() : false);
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setLastLoginAt(domain.getLastLoginAt());
        entity.setLastLoginIp(domain.getLastLoginIp());
        entity.setFailedLoginAttempts(domain.getFailedLoginAttempts() != null ? domain.getFailedLoginAttempts() : 0);
        entity.setLockedUntil(domain.getLockedUntil());
        entity.setLastPasswordChangeAt(domain.getLastPasswordChangeAt());
        entity.setTemporary(domain.getIsTemporary() != null ? domain.getIsTemporary() : false);
        entity.setPasswordChangeIntervalDays(domain.getPasswordChangeIntervalDays() != null ? domain.getPasswordChangeIntervalDays() : 90);

        if (domain.getRoles() != null) {
            entity.setRoles(domain.getRoles().stream()
                    .map(RoleMapper::toEntity)
                    .collect(Collectors.toSet()));
        }

        return entity;
    }
}
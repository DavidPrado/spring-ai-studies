package com.smart_extract_service.modules.auth.infrastructure.adapters.db;

import com.smart_extract_service.core.exception.BusinessException;
import com.smart_extract_service.modules.auth.domain.model.User;
import com.smart_extract_service.modules.auth.domain.ports.output.UserRepositoryPort;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.TenantEntity;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.UserEntity;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.mappers.UserMapper;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.specs.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserRepository jpaRepository;
    private final TenantRepository jpaTenantRepository;
    private final RoleRepository roleRepository;

    public UserPersistenceAdapter(UserRepository jpaRepository, TenantRepository jpaTenantRepository, RoleRepository roleRepository) {
        this.jpaRepository = jpaRepository;
        this.jpaTenantRepository = jpaTenantRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(UserMapper::toDomain);
    }

    @Override
    public User save(User user) {
        TenantEntity tenantEntity = user.getTenantId() != null
                ? jpaTenantRepository.findById(user.getTenantId()).orElse(null)
                : null;

        UserEntity entity = UserMapper.toEntity(user, tenantEntity);

        if (user.getId() != null) {
            jpaRepository.findById(user.getId())
                    .ifPresent(existing -> entity.setRoles(existing.getRoles()));

        } else if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            entity.setRoles(user.getRoles().stream()
                    .map(role -> roleRepository.getReferenceById(role.getId()))
                    .collect(Collectors.toSet()));
        }

        UserEntity savedEntity = jpaRepository.save(entity);
        return UserMapper.toDomain(savedEntity);
    }

    @Override
    public Page<User> findAll(Map<String, String> filters, Pageable pageable) {
        return jpaRepository.findAll(UserSpecification.filterBy(filters), pageable)
                .map(UserMapper::toDomain);
    }

    @Override
    public void deleteById(UUID userId) {
        jpaRepository.deleteById(userId);
    }

    @Override
    public void deleteAllByIdInBatch(List<UUID> userIds) {
        jpaRepository.deleteAllByIdInBatch(userIds);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public List<String> findRoleNamesByEmail(String email) {
        return jpaRepository.findRoleNamesByEmail(email);
    }

    @Override
    public void assignRoles(UUID userId, Set<UUID> roleIds) {
        UserEntity entity = jpaRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("auth.user.not_found"));
        entity.setRoles(roleIds.stream()
                .map(roleRepository::getReferenceById)
                .collect(Collectors.toSet()));
        jpaRepository.save(entity);
    }

    @Override
    public void addRoleToUser(UUID userId, UUID roleId) {
        UserEntity entity = jpaRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("auth.user.not_found"));
        entity.getRoles().add(roleRepository.getReferenceById(roleId));
        jpaRepository.save(entity);
    }

    @Override
    public List<String> findPermissionNamesByEmail(String email) {
        return jpaRepository.findPermissionNamesByEmail(email);
    }
}
package com.smart_extract_service.modules.auth.application.usecases;

import com.smart_extract_service.core.config.RestPage;
import com.smart_extract_service.core.context.Auditable;
import com.smart_extract_service.core.exception.BusinessException;
import com.smart_extract_service.core.security.JwtUtils;
import com.smart_extract_service.modules.auth.application.dto.*;
import com.smart_extract_service.modules.auth.domain.model.Role;
import com.smart_extract_service.modules.auth.domain.model.Tenant;
import com.smart_extract_service.modules.auth.domain.model.User;
import com.smart_extract_service.modules.auth.domain.ports.input.UserManagementUseCase;
import com.smart_extract_service.modules.auth.domain.ports.output.EmailOutputPort;
import com.smart_extract_service.modules.auth.domain.ports.output.ProfilePictureRepositoryPort;
import com.smart_extract_service.modules.auth.domain.ports.output.TenantRepositoryPort;
import com.smart_extract_service.modules.auth.domain.ports.output.UserRepositoryPort;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserManagementService implements UserManagementUseCase {

    private final UserRepositoryPort userRepository;
    private final ProfilePictureRepositoryPort profilePictureRepository;
    private final TenantRepositoryPort tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailOutputPort emailOutputPort;
    private final JwtUtils jwtUtils;

    public UserManagementService(
            UserRepositoryPort userRepository,
            ProfilePictureRepositoryPort profilePictureRepository,
            TenantRepositoryPort tenantRepository,
            PasswordEncoder passwordEncoder,
            EmailOutputPort emailOutputPort,
            JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.profilePictureRepository = profilePictureRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailOutputPort = emailOutputPort;
        this.jwtUtils = jwtUtils;
    }

    @Override
    @Transactional
    @Cacheable(value = "users", key = "#tenantId.toString() + #filters.hashCode() + #pageable.pageNumber")
    public Page<UserResponse> listAll(UUID tenantId, Map<String, String> filters, Pageable pageable) {

        Page<User> users = userRepository.findAll(filters, pageable);

        Set<UUID> usersWithPhotos = profilePictureRepository.findUsersWithPhotos(
                users.stream().map(User::getId).collect(Collectors.toSet())
        );

        List<UserResponse> content = users.getContent().stream()
                .map(user -> toUserResponse(user, usersWithPhotos.contains(user.getId())))
                .collect(Collectors.toList());

        return new RestPage<>(content, pageable, users.getTotalElements());
    }

    private UserResponse toUserResponse(User user, boolean hasProfilePicture) {
        Tenant tenant = user.getTenantId() != null
                ? tenantRepository.findById(user.getTenantId()).orElse(null)
                : null;

        TenantResponse tenantResponse = mapTenant(tenant);
        Set<RoleResponse> roleResponses = mapRoles(user.getRoles());

        return UserResponse.fromDomain(user, hasProfilePicture, tenantResponse, roleResponses);
    }

    private TenantResponse mapTenant(Tenant tenant) {
        if (tenant == null) return null;
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                null, // subdomain
                null, // country
                null, // currencyCode
                tenant.getActive(),
                null, // inactivationReason
                null  // blockedAt
        );
    }

    private Set<RoleResponse> mapRoles(Set<Role> roles) {
        if (roles == null) return Set.of();
        return roles.stream()
                .map(role -> new RoleResponse(
                        role.getId(),
                        role.getName(),
                        role.getDescription(),
                        role.getDisplayName(),
                        role.getIsSystemDefault(),
                        role.getPermissions() != null ? role.getPermissions().stream()
                                .map(p -> new PermissionResponse(p.getId(), p.getName(), p.getDescription()))
                                .collect(Collectors.toSet()) : Set.of()
                ))
                .collect(Collectors.toSet());
    }

    @Override
    @Auditable
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void toggleStatus(UUID userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("auth.user.not_found"));

        if (active) {
            user.activate();
        } else {
            user.deactivate();
        }

        userRepository.save(user);
    }

    @Override
    @Auditable
    @Transactional
    public void updatePassword(UUID userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("auth.user.not_found"));

        user.changePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Auditable
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }

    @Override
    @Auditable
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUsersBatch(List<UUID> userIds) {
        userRepository.deleteAllByIdInBatch(userIds);
    }

    @Override
    @Auditable
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void updateUserDetails(UUID userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("auth.user.not_found"));

        if (request.fullName() != null)              user.setFullName(request.fullName());
        if (request.email() != null)                 user.setEmail(request.email());
        if (request.phoneNumber() != null)           user.setPhoneNumber(request.phoneNumber());
        if (request.phoneNumber2() != null)          user.setPhoneNumber2(request.phoneNumber2());
        if (request.department() != null)            user.setDepartment(request.department());
        if (request.position() != null)              user.setPosition(request.position());
        if (request.costCenter() != null)            user.setCostCenter(request.costCenter());
        if (request.employeeCode() != null)          user.setEmployeeCode(request.employeeCode());
        if (request.active() != null)                user.setActive(request.active());
        if (request.requirePasswordChange() != null) user.setRequirePasswordChange(request.requirePasswordChange());
        if (request.passwordChangeIntervalDays() != null) user.setPasswordChangeIntervalDays(request.passwordChangeIntervalDays());
        if (request.isTemporary() != null)           user.setIsTemporary(request.isTemporary());
        if (request.expiresAt() != null)             user.setExpiresAt(request.expiresAt());
        user.setUpdatedAt(LocalDateTime.now());

        if (request.password() != null && !request.password().isBlank()) {
            user.changePassword(passwordEncoder.encode(request.password()));
        }

        userRepository.save(user);

        if (request.roleIds() != null) {
            userRepository.assignRoles(userId, request.roleIds());
        }
    }

    @Override
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("auth.user.not_found"));

        String resetToken = jwtUtils.generateToken(
                user.getEmail(), user.getId(), user.getTenantId(), List.of(), true
        );

        emailOutputPort.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse createUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("auth.user.already_exists");
        }

        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new BusinessException("tenant.not_found"));

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setTenantId(tenant.getId());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDepartment(request.department());
        user.setPosition(request.position());
        user.setPhoneNumber(request.phoneNumber());

        user.setActive(true);
        user.setIsTemporary(request.isTemporary() != null ? request.isTemporary() : false);
        user.setExpiresAt(request.accessExpirationDate());
        user.setRequirePasswordChange(request.mustChangePasswordNextLogin() != null ? request.mustChangePasswordNextLogin() : true);
        user.setPasswordChangeIntervalDays(request.passwordChangeIntervalDays() != null ? request.passwordChangeIntervalDays() : 90);

        user.setFailedLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastPasswordChangeAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return toUserResponse(savedUser, false);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("auth.user.not_found"));

        boolean hasPhoto = profilePictureRepository
                .findUsersWithPhotos(Set.of(userId)).contains(userId);

        return toUserResponse(user, hasPhoto);
    }
}
package com.smart_extract_service.modules.auth.domain.ports.input;

import com.smart_extract_service.modules.auth.application.dto.RegisterRequest;
import com.smart_extract_service.modules.auth.application.dto.UserResponse;
import com.smart_extract_service.modules.auth.application.dto.UserUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface UserManagementUseCase {
    Page<UserResponse> listAll(UUID tenantId, Map<String, String> filters, Pageable pageable);
    void toggleStatus(UUID userId, boolean active);
    void updatePassword(UUID userId, String newPassword);
    void deleteUser(UUID userId);
    void deleteUsersBatch(List<UUID> userIds);
    void updateUserDetails(UUID userId, UserUpdateRequest request);
    void resetPassword(String email);
    UserResponse createUser(@Valid RegisterRequest request);
    UserResponse findById(UUID userId);
}

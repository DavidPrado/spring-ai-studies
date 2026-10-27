package com.smart_extract_service.modules.auth.infrastructure.adapters.web;


import com.smart_extract_service.core.context.TenantContext;
import com.smart_extract_service.core.context.UserContext;
import com.smart_extract_service.modules.auth.application.dto.RegisterRequest;
import com.smart_extract_service.modules.auth.application.dto.UpdatePasswordRequest;
import com.smart_extract_service.modules.auth.application.dto.UserUpdateRequest;
import com.smart_extract_service.modules.auth.application.dto.UserResponse;
import com.smart_extract_service.modules.auth.domain.ports.input.UserManagementUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final UserManagementUseCase userManagementUseCase;

    public UserController(UserManagementUseCase userManagementUseCase) {
        this.userManagementUseCase = userManagementUseCase;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody @Valid RegisterRequest request) {
        UserResponse newUser = userManagementUseCase.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID userId){
        return ResponseEntity.ok(userManagementUseCase.findById(userId));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> list(
            @RequestParam Map<String, String> allParams,
            @PageableDefault(size = 10, sort = "fullName") Pageable pageable) {

        UUID tenantId = TenantContext.getCurrentTenant();
        Page<UserResponse> users = userManagementUseCase.listAll(tenantId, allParams, pageable);

        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId){
        userManagementUseCase.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteBatch(@RequestBody List<UUID> usersId){
        userManagementUseCase.deleteUsersBatch(usersId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}")
     public ResponseEntity<Void> update(@PathVariable UUID userId, @RequestBody @Valid UserUpdateRequest request){
        userManagementUseCase.updateUserDetails(userId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<Void> toggleStatus(@PathVariable UUID userId, @RequestParam boolean active) {
        userManagementUseCase.toggleStatus(userId, active);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/update-password")
    public ResponseEntity<Void> updatePassword(@RequestBody @Valid UpdatePasswordRequest request) {
        UUID currentUserId = UserContext.getUserId();
        userManagementUseCase.updatePassword(currentUserId, request.newPassword());
        return ResponseEntity.ok().build();
    }
}

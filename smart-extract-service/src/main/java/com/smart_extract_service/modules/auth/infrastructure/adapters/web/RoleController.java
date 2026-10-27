package com.smart_extract_service.modules.auth.infrastructure.adapters.web;

import com.smart_extract_service.core.config.SystemConstants;
import com.smart_extract_service.core.context.TenantContext;
import com.smart_extract_service.modules.auth.application.dto.*;
import com.smart_extract_service.modules.auth.domain.ports.input.RolePermissionUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RolePermissionUseCase rolePermissionUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('roles:read')")
    public ResponseEntity<Page<RoleSummaryResponse>> list(@RequestParam Map<String, String> allParams, @RequestParam(required = false) UUID tenantId, @PageableDefault(size = 50, sort = "name") Pageable pageable) {
        UUID currentTenant = TenantContext.getCurrentTenant();
        boolean isGlobalAdmin = SystemConstants.SYSTEM_TENANT_ID.equals(currentTenant);

        UUID effectiveTenant = (isGlobalAdmin && tenantId != null) ? tenantId : currentTenant;

        Page<RoleSummaryResponse> page = (isGlobalAdmin && tenantId == null) ?
                rolePermissionUseCase.listRolesGlobal(allParams, pageable) :
                rolePermissionUseCase.listRoles(effectiveTenant, allParams, pageable);

        return ResponseEntity.ok(page);
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('roles:read')")
    public ResponseEntity<List<PermissionResponse>> listAllPermissions() {
        return ResponseEntity.ok(rolePermissionUseCase.listAvailablePermissions());
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('roles:read')")
    public ResponseEntity<RoleResponse> getById(@PathVariable UUID roleId) {
        return ResponseEntity.ok(rolePermissionUseCase.findRoleById(roleId, TenantContext.getCurrentTenant()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('roles:write')")
    public ResponseEntity<Void> create(@RequestBody @Valid RoleRequest request) {
        rolePermissionUseCase.createRole(request.name(), TenantContext.getCurrentTenant(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('roles:write')")
    public ResponseEntity<Void> update(@PathVariable UUID roleId, @RequestBody @Valid RoleUpdateRequest request) {
        rolePermissionUseCase.updateRole(roleId, TenantContext.getCurrentTenant(), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('roles:delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID roleId) {
        rolePermissionUseCase.deleteRole(roleId, TenantContext.getCurrentTenant());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('roles:delete')")
    public ResponseEntity<Void> deleteBatch(@RequestBody List<UUID> roleIds) {
        rolePermissionUseCase.deleteRolesBatch(roleIds, TenantContext.getCurrentTenant());
        return ResponseEntity.noContent().build();
    }
}
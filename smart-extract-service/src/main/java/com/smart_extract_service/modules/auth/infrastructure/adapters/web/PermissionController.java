package com.smart_extract_service.modules.auth.infrastructure.adapters.web;

import com.smart_extract_service.modules.auth.application.dto.PermissionDetailResponse;
import com.smart_extract_service.modules.auth.domain.ports.input.PermissionConsultationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionConsultationUseCase permissionConsultationUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('roles:read')")
    public ResponseEntity<List<PermissionDetailResponse>> list() {
        return ResponseEntity.ok(permissionConsultationUseCase.listAll());
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('roles:read')")
    public ResponseEntity<List<String>> listCategories() {
        return ResponseEntity.ok(permissionConsultationUseCase.listCategories());
    }
}
package com.smart_extract_service.modules.auth.application.usecases;

import com.smart_extract_service.modules.auth.application.dto.PermissionDetailResponse;
import com.smart_extract_service.modules.auth.domain.model.Permission;
import com.smart_extract_service.modules.auth.domain.ports.input.PermissionConsultationUseCase;
import com.smart_extract_service.modules.auth.domain.ports.output.PermissionRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class PermissionConsultationService implements PermissionConsultationUseCase {

    private final PermissionRepositoryPort permissionRepository;

    public PermissionConsultationService(PermissionRepositoryPort permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional(readOnly = true)   // contrato do DatabaseTenantAspect
    public List<PermissionDetailResponse> listAll() {
        return permissionRepository.findAll().stream()
                .sorted(Comparator
                        .comparing((Permission p) -> safe(p.getCategory()))
                        .thenComparing(p -> safe(p.getName())))
                .map(p -> new PermissionDetailResponse(
                        p.getId(), p.getName(), p.getDescription(), p.getCategory()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listCategories() {
        return permissionRepository.findAll().stream()
                .map(Permission::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    private static String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
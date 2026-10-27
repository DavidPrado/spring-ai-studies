package com.smart_extract_service.modules.auth.domain.ports.input;

import com.smart_extract_service.modules.auth.application.dto.PermissionDetailResponse;

import java.util.List;

public interface PermissionConsultationUseCase {
    List<PermissionDetailResponse> listAll();
    List<String> listCategories();
}

package com.smart_extract_service.modules.auth.infrastructure.adapters.db;

import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {
}

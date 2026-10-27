package com.smart_extract_service.modules.auth.infrastructure.adapters.db;

import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends
        JpaRepository<RoleEntity, UUID>,
        JpaSpecificationExecutor<RoleEntity> {

    Optional<RoleEntity> findByIsSystemDefaultTrue();

    @Modifying
    @Transactional
    @Query("""
           DELETE FROM RoleEntity r
           WHERE r.id = :id
             AND r.tenant.id = :tenantId
             AND r.isSystemDefault = false
           """)
    void deleteByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Modifying
    @Transactional
    @Query("""
           DELETE FROM RoleEntity r
           WHERE r.id IN :ids
             AND r.tenant.id = :tenantId
             AND r.isSystemDefault = false
           """)
    void deleteAllByIdInAndTenantId(@Param("ids") List<UUID> ids,
                                    @Param("tenantId") UUID tenantId);

    @Query("""
       SELECT r.id, COUNT(p.id)
       FROM RoleEntity r LEFT JOIN r.permissions p
       WHERE r.id IN :ids
       GROUP BY r.id
       """)
    List<Object[]> countPermissionsByRoleIds(@Param("ids") List<UUID> ids);
}
package com.smart_extract_service.modules.auth.domain.ports.output;

import com.smart_extract_service.modules.auth.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface UserRepositoryPort {
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    User save(User user);
    Page<User> findAll(Map<String, String> filters, Pageable pageable);
    void deleteById(UUID userId);
    void deleteAllByIdInBatch(List<UUID> userIds);
    boolean existsByEmail(String email);
    List<String> findRoleNamesByEmail(String email);
    void assignRoles(UUID userId, Set<UUID> roleIds);
    void addRoleToUser(UUID userId, UUID roleId);
    List<String> findPermissionNamesByEmail(String email);
}

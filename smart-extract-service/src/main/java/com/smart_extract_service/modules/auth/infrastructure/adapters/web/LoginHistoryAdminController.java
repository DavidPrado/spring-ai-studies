package com.smart_extract_service.modules.auth.infrastructure.adapters.web;


import com.smart_extract_service.modules.auth.application.usecases.LoginHistoryCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/login-history")
public class LoginHistoryAdminController {

    private final LoginHistoryCleanupService cleanupService;

    public LoginHistoryAdminController(LoginHistoryCleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    @PostMapping("/cleanup")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<LoginHistoryCleanupService.CleanupResult> manualCleanup() {
        LoginHistoryCleanupService.CleanupResult result = cleanupService.cleanupOldRecords();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/cleanup/tenant/{tenantId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<LoginHistoryCleanupService.CleanupResult> cleanupByTenant(@PathVariable UUID tenantId) {
        LoginHistoryCleanupService.CleanupResult result = cleanupService.cleanupByTenant(tenantId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = cleanupService.getCleanupStatistics();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/cleanup/custom")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<LoginHistoryCleanupService.CleanupResult> customCleanup(
            @RequestParam(defaultValue = "30") int retentionDays) {
        LoginHistoryCleanupService.CleanupResult result = cleanupService.cleanupOldRecords();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> checkPending() {
        Map<String, Object> stats = cleanupService.getCleanupStatistics();

        long pending = (Long) stats.getOrDefault("old_records_30days", 0L);
        String status = pending > 0 ? "PENDING" : "CLEAN";

        return ResponseEntity.ok(Map.of(
                "status", status,
                "pending_records", pending,
                "last_check", java.time.LocalDateTime.now().toString()
        ));
    }
}
package com.smart_extract_service.modules.auth.application.usecases;

import com.smart_extract_service.core.exception.BusinessException;
import com.smart_extract_service.core.security.JwtUtils;
import com.smart_extract_service.modules.auth.application.dto.LoginRequest;
import com.smart_extract_service.modules.auth.application.dto.LoginResponse;
import com.smart_extract_service.modules.auth.domain.model.Tenant;
import com.smart_extract_service.modules.auth.domain.model.User;
import com.smart_extract_service.modules.auth.domain.model.UserLoginHistory;
import com.smart_extract_service.modules.auth.domain.ports.input.LoginUseCase;
import com.smart_extract_service.modules.auth.domain.ports.output.TenantRepositoryPort;
import com.smart_extract_service.modules.auth.domain.ports.output.UserLoginHistoryRepositoryPort;
import com.smart_extract_service.modules.auth.domain.ports.output.UserRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class IdentityService implements LoginUseCase {

    public static final UUID SYSTEM_TENANT_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;
    private static final long TOKEN_TTL_MILLIS = 86_400_000L; // 24h

    private final UserRepositoryPort userRepository;
    private final TenantRepositoryPort tenantRepository;
    private final UserLoginHistoryRepositoryPort loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final JdbcTemplate jdbcTemplate;

    public IdentityService(UserRepositoryPort userRepository,
                           TenantRepositoryPort tenantRepository,
                           UserLoginHistoryRepositoryPort loginHistoryRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtils jwtUtils,
                           JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public LoginResponse authenticate(LoginRequest request) {

        jdbcTemplate.execute(
                String.format("SET LOCAL app.current_tenant = '%s'", SYSTEM_TENANT_ID));

        log.info("Tentativa de login para email: {} de IP: {}",
                request.email(), request.ipAddress());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado: {}", request.email());
                    return new BusinessException("auth.invalid_credentials");
                });

        if (isAccountLocked(user)) {
            log.warn("Conta bloqueada para usuário: {} até: {}",
                    user.getEmail(), user.getLockedUntil());
            saveLoginHistory(user, request, false, "Account locked");
            throw new BusinessException("auth.account.locked",
                    String.format("Account locked until %s", user.getLockedUntil()));
        }

        if (Boolean.FALSE.equals(user.getActive())) {
            log.warn("Tentativa de acesso em conta desativada: {}", user.getEmail());
            saveLoginHistory(user, request, false, "User account is disabled");
            throw new BusinessException("auth.user.inactive");
        }

        if (Boolean.TRUE.equals(user.getIsTemporary()) && user.getExpiresAt() != null) {
            if (LocalDateTime.now().isAfter(user.getExpiresAt())) {
                log.warn("Acesso temporário expirado para o usuário: {}", user.getEmail());
                saveLoginHistory(user, request, false, "Temporary access expired");
                throw new BusinessException("auth.user.temporary_expired");
            }
        }

        if (user.getTenantId() != null) {
            Tenant tenant = tenantRepository.findById(user.getTenantId())
                    .orElseThrow(() -> new BusinessException("auth.tenant.not_found"));

            if (Boolean.FALSE.equals(tenant.getActive())) {
                throw new DisabledException("Este Tenant está bloqueado. Contate o administrador.");
            }
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleFailedLogin(user, request);
            log.warn("Senha inválida para usuário: {} - Tentativa {} de {}",
                    user.getEmail(), user.getFailedLoginAttempts(), MAX_FAILED_ATTEMPTS);
            throw new BusinessException("auth.password.invalid");
        }

        return handleSuccessfulLogin(user, request);
    }

    // ── Sucesso ──────────────────────────────────────────────────────────
    private LoginResponse handleSuccessfulLogin(User user, LoginRequest request) {

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(request.ipAddress());
        userRepository.save(user);

        saveLoginHistory(user, request, true, null);

        log.info("Login bem-sucedido para usuário: {} de IP: {}",
                user.getEmail(), request.ipAddress());

        // ✅ CORAÇÃO DA CORREÇÃO:
        // authorities = roles (ROLE_*) + permissões granulares (roles:read, users:write...)
        // Isso faz o @PreAuthorize("hasAuthority('roles:read')") funcionar no JWT.
        List<String> roleNames = userRepository.findRoleNamesByEmail(user.getEmail());
        List<String> permissionNames = userRepository.findPermissionNamesByEmail(user.getEmail());

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        roleNames.stream()
                .map(this::normalizeRole)          // garante prefixo ROLE_
                .distinct()
                .forEach(name -> authorities.add(new SimpleGrantedAuthority(name)));

        permissionNames.stream()                    // 'roles:read' entra SEM prefixo
                .distinct()
                .forEach(name -> authorities.add(new SimpleGrantedAuthority(name)));

        UUID tenantId = user.getTenantId();

        var token = jwtUtils.generateToken(
                user.getEmail(),
                user.getId(),
                tenantId,
                authorities,
                user.getRequirePasswordChange() != null && user.getRequirePasswordChange()
        );

        List<String> normalizedRoles = authorities.stream()
                .map(SimpleGrantedAuthority::getAuthority)
                .sorted()
                .toList();

        return new LoginResponse(
                token,
                "Bearer",
                TOKEN_TTL_MILLIS,
                user.getFullName(),
                normalizedRoles,
                tenantId
        );
    }

    // ── Falha / bloqueio ─────────────────────────────────────────────────
    private boolean isAccountLocked(User user) {
        if (user.getLockedUntil() == null) return false;

        if (LocalDateTime.now().isAfter(user.getLockedUntil())) {
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            return false;
        }
        return true;
    }

    private void handleFailedLogin(User user, LoginRequest request) {
        int attempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            log.warn("Usuário {} bloqueado por {} minutos devido a {} tentativas falhas",
                    user.getEmail(), LOCK_DURATION_MINUTES, attempts);
        }

        userRepository.save(user);
        saveLoginHistory(user, request, false, "Invalid password");
    }

    private String normalizeRole(String roleName) {
        return roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
    }

    private void saveLoginHistory(User user, LoginRequest request,
                                  boolean successful, String failureReason) {
        UserLoginHistory history = UserLoginHistory.builder()
                .user(user)
                .loginAt(LocalDateTime.now())
                .ipAddress(request.ipAddress())
                .userAgent(request.userAgent())
                .successful(successful)
                .failureReason(failureReason)
                .build();

        loginHistoryRepository.save(history);
    }
}
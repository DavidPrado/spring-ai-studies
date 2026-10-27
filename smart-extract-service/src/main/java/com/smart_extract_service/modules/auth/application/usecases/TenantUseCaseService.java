package com.smart_extract_service.modules.auth.application.usecases;

import com.smart_extract_service.core.exception.BusinessException;
import com.smart_extract_service.modules.auth.application.dto.RegisterRequest;
import com.smart_extract_service.modules.auth.application.dto.TenantRequest;
import com.smart_extract_service.modules.auth.application.dto.TenantResponse;
import com.smart_extract_service.modules.auth.domain.model.Tenant;
import com.smart_extract_service.modules.auth.domain.ports.input.TenantUseCase;
import com.smart_extract_service.modules.auth.domain.ports.input.UserManagementUseCase;
import com.smart_extract_service.modules.auth.domain.ports.output.TenantRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.smart_extract_service.core.config.SystemConstants.SYSTEM_TENANT_ID;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TenantUseCaseService implements TenantUseCase {

    private final TenantRepositoryPort tenantRepository;
    private final UserManagementUseCase userManagementUseCase;


    public TenantUseCaseService(TenantRepositoryPort tenantRepository, UserManagementUseCase userManagementUseCase) {
        this.tenantRepository = tenantRepository;
        this.userManagementUseCase = userManagementUseCase;
    }

    @Override
    @Transactional
    public TenantResponse createTenant(TenantRequest request) {
        if (tenantRepository.existsByName(request.name())) {
            throw new BusinessException("auth.tenant.already.exists");
        }

        Tenant tenant = new Tenant();
        tenant.setId(UUID.randomUUID());
        tenant.setName(request.name());
        tenant.setSubdomain(request.subdomain());
        tenant.setCountry(request.country());
        tenant.setCurrencyCode(request.currencyCode());
        tenant.setActive(true);
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());

        Tenant saved = tenantRepository.save(tenant);
        userManagementUseCase.createUser(createUserDefault(saved.getName(), saved.getId()));

        return mapToResponse(saved);
    }

    private RegisterRequest createUserDefault(String name, UUID idTenant) {
        return new RegisterRequest(
                name,
                name.toLowerCase().replaceAll("\\s+", "") + "@erpiasmart.com",
                name.toLowerCase().replaceAll("\\s+", "") + "admin",
                idTenant,
                null, // phoneNumber
                null, // phoneNumber2
                null, // department
                null, // position
                null, // costCenter
                null, // employeeCode
                false,// isTemporary
                null, // accessExpirationDate
                null, // passwordChangeIntervalDays
                false // mustChangePasswordNextLogin
        );
    }

    @Override
    @Transactional
    public void updateTenant(UUID id, TenantRequest request) {
        Tenant tenant = loadTenant(id);

        if (id.equals(SYSTEM_TENANT_ID)) {
            throw new BusinessException("system.tenant.cannot.be.disabled");
        }

        if (!tenant.getName().equals(request.name()) && tenantRepository.existsByName(request.name())) {
            throw new BusinessException("auth.tenant.already.exists");
        }

        tenant.setName(request.name());
        tenant.setCountry(request.country());
        tenant.setCurrencyCode(request.currencyCode());
        tenant.setSubdomain(request.subdomain());
        tenant.setActive(request.active());
        tenant.setBlockedAt(request.blockedAt());
        tenant.setInactivationReason(request.inactivationReason());
        tenant.setUpdatedAt(LocalDateTime.now());

        tenantRepository.save(tenant);
    }

    @Override
    @Transactional
    public void updateStatus(UUID id, TenantRequest request) {
        if (id.equals(SYSTEM_TENANT_ID)) {
            throw new BusinessException("system.tenant.cannot.be.disabled");
        }

        Tenant tenant = loadTenant(id);
        tenant.setActive(request.active());

        if (!Boolean.TRUE.equals(request.active())) {
            tenant.setBlockedAt(LocalDateTime.now());
            tenant.setInactivationReason(request.inactivationReason());
        } else {
            tenant.setBlockedAt(null);
            tenant.setInactivationReason(null);
        }
        tenant.setUpdatedAt(LocalDateTime.now());

        tenantRepository.save(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TenantResponse> findAll(String name, Boolean active, Pageable pageable) {
        return tenantRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse findById(UUID id) {
        return mapToResponse(loadTenant(id));
    }

    private TenantResponse mapToResponse(Tenant t) {
        return new TenantResponse(
                t.getId(),
                t.getName(),
                t.getSubdomain(),
                t.getCountry(),
                t.getCurrencyCode(),
                Boolean.TRUE.equals(t.getActive()),
                t.getInactivationReason(),
                t.getBlockedAt()
        );
    }

    private Tenant loadTenant(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new BusinessException("tenant.not.found"));
    }
}
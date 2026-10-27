package com.smart_extract_service.modules.auth.domain.model;

import java.util.Set;
import java.util.UUID;

public class Role {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String displayName;
    private String description;
    private Boolean isSystemDefault;
    private Boolean isEditable;
    private Set<Permission> permissions;

    public Role() {}

    public Role(UUID id, UUID tenantId, String name, String displayName, String description, Boolean isSystemDefault, Boolean isEditable, Set<Permission> permissions) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.isSystemDefault = isSystemDefault;
        this.isEditable = isEditable;
        this.permissions = permissions;
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; } // 👈 Getter para TenantId
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; } // 👈 Setter para TenantId

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsSystemDefault() { return isSystemDefault; }
    public void setIsSystemDefault(Boolean isSystemDefault) { this.isSystemDefault = isSystemDefault; }

    public Boolean getIsEditable() { return isEditable; }
    public void setIsEditable(Boolean isEditable) { this.isEditable = isEditable; }

    public Set<Permission> getPermissions() { return permissions; }
    public void setPermissions(Set<Permission> permissions) { this.permissions = permissions; }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
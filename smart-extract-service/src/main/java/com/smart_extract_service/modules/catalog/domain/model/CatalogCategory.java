package com.smart_extract_service.modules.catalog.domain.model;

import com.smart_extract_service.modules.catalog.domain.enums.Vertical;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CatalogCategory {
    private UUID id;
    private UUID tenantId;
    private UUID parentId;
    private String name;
    private Vertical vertical;
    private boolean active;
    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime updateAt;
    private UUID updatedBy;

    public boolean isRoot(){ return parentId == null;}

}

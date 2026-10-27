package com.smart_extract_service.modules.auth.domain.model;

import java.util.UUID;

public class Permission {


    private UUID id;
    private String name;
    private String description;
    private String category;   // ← ADICIONAR (se não existir)

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}

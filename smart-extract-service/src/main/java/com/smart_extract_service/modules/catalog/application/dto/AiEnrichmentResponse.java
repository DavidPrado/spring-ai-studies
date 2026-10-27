package com.smart_extract_service.modules.catalog.application.dto;

import java.util.List;

public record AiEnrichmentResponse(

        String category,
        List<String> tags,
        String enhancedDescription


) {}

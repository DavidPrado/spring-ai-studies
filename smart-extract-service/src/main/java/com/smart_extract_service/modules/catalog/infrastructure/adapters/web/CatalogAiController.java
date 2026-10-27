package com.smart_extract_service.modules.catalog.infrastructure.adapters.web;


import com.smart_extract_service.modules.catalog.application.dto.AiEnrichmentResponse;
import com.smart_extract_service.modules.catalog.application.usecases.CatalogAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/catalog/items/ai")
@RequiredArgsConstructor
public class CatalogAiController {

    private final CatalogAiService iaService;

    @PostMapping("/enrich")
    public ResponseEntity<AiEnrichmentResponse> enrichProduct(@RequestBody Map<String, String> request){
        String name = request.getOrDefault("name", "");
        String description = request.getOrDefault("description", "");

        AiEnrichmentResponse response = iaService.enrichProduct(name, description);
        return  ResponseEntity.ok(response);
    }

    @PostMapping("/classify")
    public ResponseEntity<Map<String, String>> classifyProduct(@RequestBody Map<String, String> request){
        String name = request.getOrDefault("name", "");
        String type = iaService.classifyProductType(name);

        return ResponseEntity.ok(Map.of("type", type));
    }

    @PostMapping("/extract-sku")
    public ResponseEntity<Map<String, String>> extractSku(@RequestBody Map<String, String> request){
        String text = request.getOrDefault("text", "");
        String sku = iaService.extractSku(text);

        if(sku == null){
            return ResponseEntity.badRequest().body(Map.of("error", "SKU não encontrado no texto"));
        }
 
        return ResponseEntity.ok(Map.of("sku", sku));
    }

}

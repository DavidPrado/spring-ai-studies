package com.smart_extract_service.modules.auth.infrastructure.adapters.web;

import com.smart_extract_service.core.config.JsonMessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/i18n")
public class I18nController {

    private final JsonMessageSource messageSource;

    public I18nController(JsonMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @GetMapping("/{language}")
    public ResponseEntity<Map<String, String>> getTranslations(
            @PathVariable String language
    ) {
        Locale locale = Locale.forLanguageTag(language);
        Map<String, String> translations = messageSource.getAllMessages(locale);
        return ResponseEntity.ok(translations);
    }

    @GetMapping("/languages")
    public ResponseEntity<List<Map<String, String>>> getLanguages() {
        List<Map<String, String>> languages = Arrays.asList(
                Map.of("code", "pt", "name", "Português", "flag", "🇧🇷"),
                Map.of("code", "en", "name", "English", "flag", "🇺🇸"),
                Map.of("code", "es", "name", "Español", "flag", "🇪🇸"),
                Map.of("code", "zh", "name", "中文", "flag", "🇨🇳"),
                Map.of("code", "hi", "name", "हिन्दी", "flag", "🇮🇳"),
                Map.of("code", "ar", "name", "العربية", "flag", "🇸🇦")
        );
        return ResponseEntity.ok(languages);
    }
}

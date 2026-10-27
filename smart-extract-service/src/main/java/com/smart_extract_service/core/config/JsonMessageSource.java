// src/main/java/com/smart_extract_service/core/config/JsonMessageSource.java
package com.smart_extract_service.core.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;

@Component("messageSource")
public class JsonMessageSource extends AbstractMessageSource {

    private Map<String, Map<String, String>> messages;

    @PostConstruct
    public void loadMessages() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Resource resource = new ClassPathResource("i18n/messages.json");

        try (InputStream inputStream = resource.getInputStream()) {
            this.messages = mapper.readValue(inputStream,
                    new TypeReference<Map<String, Map<String, String>>>() {});
        }
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        if (messages == null) {
            return new MessageFormat(code, locale);
        }

        String language = locale.getLanguage();

        Map<String, String> translations = messages.get(code);
        if (translations == null) {
            return new MessageFormat(code, locale);
        }

        String message = translations.getOrDefault(language,
                translations.containsKey("pt") ? translations.get("pt") : code);

        if (message == null) {
            return new MessageFormat(code, locale);
        }

        return new MessageFormat(message, locale);
    }

    public Map<String, String> getAllMessages(Locale locale) {
        if (messages == null) return Map.of();

        String language = locale.getLanguage();
        java.util.HashMap<String, String> result = new java.util.HashMap<>();

        messages.forEach((code, translations) -> {
            String message = translations.getOrDefault(language,
                    translations.get("pt"));
            if (message != null) {
                result.put(code, message);
            }
        });

        return result;
    }
}
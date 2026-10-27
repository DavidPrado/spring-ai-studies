package com.smart_extract_service.modules.catalog.domain.model;

import com.smart_extract_service.core.exception.BusinessException;
import com.smart_extract_service.modules.catalog.domain.enums.AppliesTo;
import com.smart_extract_service.modules.catalog.domain.enums.DataType;
import com.smart_extract_service.modules.catalog.domain.enums.ItemType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AttributeDef {
    private UUID id;
    private UUID tenantId;
    private UUID categoryId;
    /** Chave técnica gravada no extra_data (ex.: "registry_number"). */
    private String key;
    /** Rótulo amigável exibido no formulário (ex.: "Registro ANVISA"). */
    private String label;
    private DataType dataType;
    private String unit;
    /** Valores permitidos quando dataType = ENUM. */
    private List<String> options;
    private boolean required;
    private AppliesTo appliesTo;
    private int sortOrder;

    private UUID createdBy;
    private LocalDateTime updatedAt;

    public boolean appliesTo(ItemType type) {
        return appliesTo == AppliesTo.BOTH || appliesTo.name().equals(type.name());
    }

    public void validateValue(Object value) {
        boolean empty = value == null || (value instanceof String s && s.isBlank());

        if (required && empty) {
            throw new BusinessException("catalog.extra.required",
                    "O campo \"%s\" é obrigatório para esta categoria.".formatted(label));
        }
        if (empty) return;

        switch (dataType) {
            case NUMBER -> {
                if (!(value instanceof Number)) {
                    throw new BusinessException("catalog.extra.type",
                            "O campo \"%s\" deve ser numérico.".formatted(label));
                }
            }
            case BOOLEAN -> {
                if (!(value instanceof Boolean)) {
                    throw new BusinessException("catalog.extra.type",
                            "O campo \"%s\" deve ser sim/não.".formatted(label));
                }
            }
            case ENUM -> {
                if (options == null || !options.contains(String.valueOf(value))) {
                    throw new BusinessException("catalog.extra.option",
                            "O campo \"%s\" deve ser um destes valores: %s."
                                    .formatted(label, String.join(", ", options == null ? List.of() : options)));
                }
            }
            case DATE -> {
                try {
                    LocalDate.parse(String.valueOf(value));
                } catch (Exception e) {
                    throw new BusinessException("catalog.extra.date",
                            "O campo \"%s\" deve ser uma data no formato AAAA-MM-DD.".formatted(label));
                }
            }
            case TEXT -> { /* texto livre */ }
        }
    }
}
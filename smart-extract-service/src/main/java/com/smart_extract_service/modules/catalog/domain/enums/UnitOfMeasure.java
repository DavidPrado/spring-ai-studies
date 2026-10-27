package com.smart_extract_service.modules.catalog.domain.enums;

import lombok.Getter;

@Getter
public enum UnitOfMeasure {
    UN("Unidade"), KG("Quilograma"), G("Grama"), L("Litro"), ML("Mililitro"),
    M("Metro"), M2("Metro quadrado"), M3("Metro cúbico"),
    HR("Hora"), CX("Caixa"), PC("Pacote");

    private final String label;

    UnitOfMeasure(String label) { this.label = label; }
}

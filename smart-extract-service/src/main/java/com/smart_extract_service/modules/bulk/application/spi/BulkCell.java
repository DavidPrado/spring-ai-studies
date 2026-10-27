package com.smart_extract_service.modules.bulk.application.spi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class BulkCell {


    public enum Type {
        TEXT, NUMBER, PERCENT, DATE, DATETIME, BOOLEAN
    }

    private final Type type;
    private final Object value;


    private BulkCell(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public static BulkCell text(String value) {
        return new BulkCell(Type.TEXT, value);
    }

    public static BulkCell number(BigDecimal value) {
        return new BulkCell(Type.NUMBER, value);
    }

    public static BulkCell percent(BigDecimal value) {
        return new BulkCell(Type.PERCENT, value);
    }

    public static BulkCell date(LocalDate value) {
        return new BulkCell(Type.DATE, value);
    }

    public static BulkCell dateTime(LocalDateTime value) {
        return new BulkCell(Type.DATETIME, value);
    }

    public static BulkCell bool(Boolean value) {
        return new BulkCell(Type.BOOLEAN, value);
    }

    public Type getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public String asFormattedString() {
        if (value == null) return "";
        return switch (type) {
            case TEXT -> value.toString();
            case NUMBER, PERCENT -> {
                BigDecimal bd = (BigDecimal) value;
                String formatted = bd.setScale(2, java.math.RoundingMode.HALF_UP)
                        .toString()
                        .replace('.', ',');
                yield type == Type.PERCENT ? formatted + "%" : formatted;
            }
            case DATE -> {
                LocalDate d = (LocalDate) value;
                yield String.format("%02d/%02d/%04d", d.getDayOfMonth(),
                        d.getMonthValue(), d.getYear());
            }
            case DATETIME -> {
                LocalDateTime dt = (LocalDateTime) value;
                yield String.format("%02d/%02d/%04d %02d:%02d",
                        dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear(),
                        dt.getHour(), dt.getMinute());
            }
            case BOOLEAN -> Boolean.TRUE.equals(value) ? "Sim" : "Não";
        };

    }
}

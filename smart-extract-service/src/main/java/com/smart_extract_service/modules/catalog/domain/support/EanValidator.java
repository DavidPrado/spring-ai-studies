package com.smart_extract_service.modules.catalog.domain.support;

public final class EanValidator {

    private EanValidator() {}

    public static boolean isValid(String ean) {
        if (ean == null) return false;
        String digits = ean.trim();
        if (!digits.matches("\\d{13}|\\d{14}")) return false;

        int sum = 0;
        int length = digits.length();
        for (int i = 0; i < length - 1; i++) {
            // ✅ CORREÇÃO: posição par (0,2,4...) → peso 1, ímpar (1,3,5...) → peso 3
            int weight = (i % 2 == 0) ? 1 : 3;
            sum += (digits.charAt(i) - '0') * weight;
        }
        int check = (10 - (sum % 10)) % 10;
        return check == (digits.charAt(length - 1) - '0');
    }
}
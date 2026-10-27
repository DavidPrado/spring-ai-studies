package com.smart_extract_service.core.context;

import java.util.UUID;

public class UserContext {
    private static final ThreadLocal<UUID> userIdThreadLocal = new ThreadLocal<>();

    public static void setUserId(UUID userId) {
        userIdThreadLocal.set(userId);
    }

    public static UUID getUserId() {
        return userIdThreadLocal.get();
    }

    public static void clear() {
        userIdThreadLocal.remove();
    }
}

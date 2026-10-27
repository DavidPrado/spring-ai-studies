package com.smart_extract_service.modules.auth.domain.ports.output;

public interface EmailOutputPort {
    void sendPasswordResetEmail(String to, String token);
}

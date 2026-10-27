package com.smart_extract_service.modules.auth.domain.ports.input;

import com.smart_extract_service.modules.auth.application.dto.LoginRequest;
import com.smart_extract_service.modules.auth.application.dto.LoginResponse;

public interface LoginUseCase {
    LoginResponse authenticate(LoginRequest request);
}

package com.smart_extract_service.modules.auth.infrastructure.adapters.web;


import com.smart_extract_service.modules.auth.application.dto.LoginRequest;
import com.smart_extract_service.modules.auth.application.dto.LoginResponse;
import com.smart_extract_service.modules.auth.domain.ports.input.LoginUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        String clientIp = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginRequest enrichedRequest = new LoginRequest(
                request.email(),
                request.password(),
                clientIp,
                userAgent
        );

        LoginResponse loginResponse = loginUseCase.authenticate(enrichedRequest);

        ResponseCookie cookie = ResponseCookie.from("accessToken", loginResponse.token())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(loginResponse.expiresIn() / 1000)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        LoginResponse safeResponse = new LoginResponse(
                null,
                loginResponse.type(),
                loginResponse.expiresIn(),
                loginResponse.userName(),
                loginResponse.roles(),
                loginResponse.tenantId()
        );

        return ResponseEntity.ok(safeResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

}

package com.smart_extract_service.core.security;

import com.smart_extract_service.core.context.TenantContext;
import com.smart_extract_service.core.context.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j; // ← Adicione este import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j // ← Adicione esta anotação
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    private static final List<String> ALLOWED_PATHS_WHEN_MUST_CHANGE_PASSWORD = List.of(
            "/v1/auth/change-password",
            "/v1/auth/logout",
            "/v1/users/change-password"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 1. DIAGNÓSTICO: Verificar se os cookies estão chegando no backend
        Cookie[] cookies = request.getCookies();
        log.info("=== [JWT FILTER] URI: {} | Cookies recebidos: {}", requestURI, cookies != null ? cookies.length : 0);

        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("accessToken".equals(c.getName())) {
                    log.info("=== [JWT FILTER] Cookie 'accessToken' ENCONTRADO! Tamanho do token: {}", c.getValue().length());
                }
            }
        }

        String token = recoverToken(request);

        if (token != null) {
            // 2. DIAGNÓSTICO: Verificar se a validação está passando
            boolean isValid = jwtUtils.validateToken(token);
            log.info("=== [JWT FILTER] Resultado de jwtUtils.validateToken(token): {}", isValid);

            if (isValid) {
                boolean mustChangePassword = jwtUtils.isMustChangePasswordFromToken(token);

                if (mustChangePassword && !isAllowedPath(requestURI)) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write(
                            "{\"error\":\"PasswordChangeRequired\",\"message\":\"Você precisa alterar sua senha para continuar acessando o sistema.\"}"
                    );
                    return;
                }

                UUID tenantId = jwtUtils.getTenantIdFromToken(token);
                UUID userId = jwtUtils.getUserIdFromToken(token);
                String email = jwtUtils.getEmailFromToken(token);
                var authorities = jwtUtils.getAuthoritiesFromToken(token);

                TenantContext.setCurrentTenant(tenantId);
                UserContext.setUserId(userId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("=== [JWT FILTER] Autenticação definida com sucesso para: {}", email);
            } else {
                log.warn("=== [JWT FILTER] Token presente, mas INVÁLIDO ou EXPIRADO.");
            }
        } else {
            log.warn("=== [JWT FILTER] Token NÃO encontrado nos cookies.");
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            UserContext.clear();
        }
    }

    private boolean isAllowedPath(String requestURI) {
        return ALLOWED_PATHS_WHEN_MUST_CHANGE_PASSWORD.stream().anyMatch(requestURI::startsWith);
    }

    private String recoverToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
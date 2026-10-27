package com.smart_extract_service.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    @Value("${app.jwt.secret:default-secret-key}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}")
    private Long jwtExpiration;

    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, UUID userId, UUID tenantId, Collection<? extends GrantedAuthority> authorities, boolean mustChangePassword){
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(email)
                .id(UUID.randomUUID().toString())
                .claim("userId", userId.toString()) // ADICIONADO: Claim do Usuário
                .claim("tenantId", tenantId.toString())
                .claim("roles", roles)
                .claim("mustChange", mustChangePassword)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token){
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean isMustChangePasswordFromToken(String token){
        Boolean mustChange = getClaims(token).get("mustChange", Boolean.class);
        return Boolean.TRUE.equals(mustChange);
    }

    public String getEmailFromToken(String token){
        return getClaims(token).getSubject();
    }

    public UUID getTenantIdFromToken(String token) {
        String tenantId = getClaims(token).get("tenantId", String.class);
        return UUID.fromString(tenantId);
    }

    public UUID getUserIdFromToken(String token) {
        String userId = getClaims(token).get("userId", String.class);
        return UUID.fromString(userId);
    }

    public List<SimpleGrantedAuthority> getAuthoritiesFromToken(String token) {
        List<String> roles = (List<String>) getClaims(token).get("roles");
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getTokenIdFromToken(String token) {
        return getClaims(token).getId();
    }
}

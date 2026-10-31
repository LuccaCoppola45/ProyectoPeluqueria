package com.example.ProyectoPeluqueria.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    private final SecretKey signInKey;

    public JwtService(@Value("${jwt.secret:}") String secret) {
        this.signInKey = buildKey(secret);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        userDetails.getAuthorities().stream()
                .findFirst()
                .ifPresent(authority -> claims.put("role", authority.getAuthority()));
        return generateToken(claims, userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + duracionSesionMs(userDetails)))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Duración de la sesión: el admin 7 días (para no loguearse a cada rato), el resto 24 horas
    private static final long DURACION_ADMIN_MS = 7L * 24 * 60 * 60 * 1000;
    private static final long DURACION_CLIENTE_MS = 24L * 60 * 60 * 1000;

    private long duracionSesionMs(UserDetails userDetails) {
        boolean esAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> "ADMIN".equals(a.getAuthority()) || "ROLE_ADMIN".equals(a.getAuthority()));
        return esAdmin ? DURACION_ADMIN_MS : DURACION_CLIENTE_MS;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        return signInKey;
    }

    /**
     * La clave sale de la variable de entorno JWT_SECRET (nunca del código, el repo es público).
     * Acepta Base64 o texto plano; tiene que tener al menos 32 bytes.
     * Si no está definida (desarrollo local) se genera una aleatoria: los logins se invalidan al reiniciar.
     */
    private static SecretKey buildKey(String secret) {
        if (secret == null || secret.isBlank()) {
            log.warn("JWT_SECRET no está definida: se usa una clave aleatoria (solo para desarrollo local)");
            return Jwts.SIG.HS256.key().build();
        }
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret.trim());
        } catch (RuntimeException e) {
            keyBytes = secret.trim().getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET es muy corta: tiene que tener al menos 32 bytes");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
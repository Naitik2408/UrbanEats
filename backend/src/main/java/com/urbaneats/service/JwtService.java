package com.urbaneats.service;

import com.urbaneats.exception.WeakJwtSecretException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for JWT token generation, validation, and claims extraction.
 * Uses JJWT library with HS256 algorithm.
 * Validates JWT secret strength on startup.
 */
@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    private static final int MIN_SECRET_LENGTH = 32; // 256 bits

    /**
     * Validate JWT secret on startup.
     * Ensures the secret is strong enough for HS256 algorithm.
     */
    @PostConstruct
    public void validateJwtSecret() {
        if (jwtSecret == null || jwtSecret.length() < MIN_SECRET_LENGTH) {
            String message = String.format(
                "JWT secret must be at least %d characters (256 bits) for HS256 algorithm. Current length: %d",
                MIN_SECRET_LENGTH,
                jwtSecret != null ? jwtSecret.length() : 0
            );
            log.error(message);
            throw new WeakJwtSecretException(message);
        }
        
        if (log.isInfoEnabled()) {
            log.info("JWT secret validated successfully (length: {} characters)", jwtSecret.length());
        }
    }

    /**
     * Generate JWT token for a user.
     *
     * @param userId the user ID
     * @param role the user role (ROLE_ADMIN or ROLE_CUSTOMER)
     * @return the generated JWT token
     */
    public String generateToken(Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        return createToken(claims, userId.toString());
    }

    /**
     * Create JWT token with claims and subject.
     *
     * @param claims additional claims
     * @param subject the subject (userId)
     * @return the JWT token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validate JWT token.
     *
     * @param token the JWT token
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract username (userId) from token.
     *
     * @param token the JWT token
     * @return the username (userId)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract userId from token.
     *
     * @param token the JWT token
     * @return the user ID
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Extract role from token.
     *
     * @param token the JWT token
     * @return the role
     */
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Extract expiration date from token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Check if token is expired.
     *
     * @param token the JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract a specific claim from token.
     *
     * @param token the JWT token
     * @param claimsResolver function to extract the claim
     * @param <T> the type of the claim
     * @return the claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token.
     *
     * @param token the JWT token
     * @return the claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get the signing key for JWT.
     *
     * @return the signing key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

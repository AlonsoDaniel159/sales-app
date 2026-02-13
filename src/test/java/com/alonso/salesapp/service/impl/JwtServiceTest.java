package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.exception.InvalidTokenException;
import com.alonso.salesapp.model.Role;
import com.alonso.salesapp.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Tests")
public class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;
    private String secret;
    private Long expiration;
    private Long refreshExpiration;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        secret = "MySecretKeyForJWTTokenGeneration2026ThisIsAVeryLongSecretKeyThatShouldBeAtLeast256BitsLong";
        expiration = 3600000L; // 1 hora
        refreshExpiration = 86400000L; // 24 horas

        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expiration", expiration);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", refreshExpiration);

        Role role = Role.builder()
                .idRole(1)
                .name("ADMIN")
                .enabled(true)
                .build();

        userDetails = User.builder()
                .idUser(1)
                .username("testuser")
                .password("password123")
                .role(role)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Should generate access token successfully")
    void shouldGenerateAccessToken() {
        // When
        String token = jwtService.generateAccessToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals("testuser", jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("Should generate refresh token successfully")
    void shouldGenerateRefreshToken() {
        // When
        String token = jwtService.generateRefreshToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals("testuser", jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsername() {
        // Given
        String token = jwtService.generateAccessToken(userDetails);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void shouldExtractExpiration() {
        // Given
        String token = jwtService.generateAccessToken(userDetails);

        // When
        Date expirationDate = jwtService.extractExpiration(token);

        // Then
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    @DisplayName("Should validate token successfully")
    void shouldValidateToken() {
        // Given
        String token = jwtService.generateAccessToken(userDetails);

        // When
        Boolean isValid = jwtService.validateToken(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject token for different user")
    void shouldRejectTokenForDifferentUser() {
        // Given
        String token = jwtService.generateAccessToken(userDetails);

        Role role = Role.builder()
                .idRole(2)
                .name("USER")
                .enabled(true)
                .build();

        UserDetails differentUser = User.builder()
                .idUser(2)
                .username("differentuser")
                .password("password456")
                .role(role)
                .enabled(true)
                .build();

        // When
        Boolean isValid = jwtService.validateToken(token, differentUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException for expired token")
    void shouldThrowExceptionForExpiredToken() {
        // Given - crear un token ya expirado
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date(System.currentTimeMillis() - 10000))
                .expiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(key)
                .compact();

        // When & Then
        assertThrows(InvalidTokenException.class, () -> {
            jwtService.extractUsername(expiredToken);
        });
    }

    @Test
    @DisplayName("Should throw InvalidTokenException for malformed token")
    void shouldThrowExceptionForMalformedToken() {
        // Given
        String malformedToken = "not.a.valid.token";

        // When & Then
        assertThrows(InvalidTokenException.class, () -> {
            jwtService.extractUsername(malformedToken);
        });
    }

    @Test
    @DisplayName("Should throw InvalidTokenException for token with invalid signature")
    void shouldThrowExceptionForInvalidSignature() {
        // Given - crear un token con una clave diferente
        String differentSecret = "DifferentSecretKeyThatIsAlsoVeryLongAndSecureForJWTTokenGeneration2026AtLeast256Bits";
        SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));

        String tokenWithDifferentSignature = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(differentKey)
                .compact();

        // When & Then
        assertThrows(InvalidTokenException.class, () -> {
            jwtService.extractUsername(tokenWithDifferentSignature);
        });
    }

    @Test
    @DisplayName("Should generate different tokens for access and refresh")
    void shouldGenerateDifferentTokens() {
        // When
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Then
        assertNotEquals(accessToken, refreshToken);

        // Ambos deben tener el mismo username
        assertEquals(jwtService.extractUsername(accessToken), jwtService.extractUsername(refreshToken));

        // Pero diferentes tiempos de expiración
        Date accessExpiration = jwtService.extractExpiration(accessToken);
        Date refreshExpiration = jwtService.extractExpiration(refreshToken);
        assertTrue(refreshExpiration.after(accessExpiration));
    }
    
}

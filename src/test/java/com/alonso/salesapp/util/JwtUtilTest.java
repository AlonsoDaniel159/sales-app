package com.alonso.salesapp.util;

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

@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;
    private String secret;
    private Long expiration;
    private Long refreshExpiration;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        secret = "MySecretKeyForJWTTokenGeneration2026ThisIsAVeryLongSecretKeyThatShouldBeAtLeast256BitsLong";
        expiration = 3600000L; // 1 hora
        refreshExpiration = 86400000L; // 24 horas

        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", refreshExpiration);

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
        String token = jwtUtil.generateAccessToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals("testuser", jwtUtil.extractUsername(token));
    }

    @Test
    @DisplayName("Should generate refresh token successfully")
    void shouldGenerateRefreshToken() {
        // When
        String token = jwtUtil.generateRefreshToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals("testuser", jwtUtil.extractUsername(token));
    }

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsername() {
        // Given
        String token = jwtUtil.generateAccessToken(userDetails);

        // When
        String username = jwtUtil.extractUsername(token);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void shouldExtractExpiration() {
        // Given
        String token = jwtUtil.generateAccessToken(userDetails);

        // When
        Date expirationDate = jwtUtil.extractExpiration(token);

        // Then
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    @DisplayName("Should validate token successfully")
    void shouldValidateToken() {
        // Given
        String token = jwtUtil.generateAccessToken(userDetails);

        // When
        Boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject token for different user")
    void shouldRejectTokenForDifferentUser() {
        // Given
        String token = jwtUtil.generateAccessToken(userDetails);

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
        Boolean isValid = jwtUtil.validateToken(token, differentUser);

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
            jwtUtil.extractUsername(expiredToken);
        });
    }

    @Test
    @DisplayName("Should throw InvalidTokenException for malformed token")
    void shouldThrowExceptionForMalformedToken() {
        // Given
        String malformedToken = "not.a.valid.token";

        // When & Then
        assertThrows(InvalidTokenException.class, () -> {
            jwtUtil.extractUsername(malformedToken);
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
            jwtUtil.extractUsername(tokenWithDifferentSignature);
        });
    }

    @Test
    @DisplayName("Should generate different tokens for access and refresh")
    void shouldGenerateDifferentTokens() {
        // When
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // Then
        assertNotEquals(accessToken, refreshToken);

        // Ambos deben tener el mismo username
        assertEquals(jwtUtil.extractUsername(accessToken), jwtUtil.extractUsername(refreshToken));

        // Pero diferentes tiempos de expiración
        Date accessExpiration = jwtUtil.extractExpiration(accessToken);
        Date refreshExpiration = jwtUtil.extractExpiration(refreshToken);
        assertTrue(refreshExpiration.after(accessExpiration));
    }
}


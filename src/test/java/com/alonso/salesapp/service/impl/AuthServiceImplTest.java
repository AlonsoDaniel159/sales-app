package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.dto.auth.AuthResponse;
import com.alonso.salesapp.dto.auth.LoginRequest;
import com.alonso.salesapp.dto.auth.RefreshTokenRequest;
import com.alonso.salesapp.dto.auth.RegisterRequest;
import com.alonso.salesapp.exception.InvalidTokenException;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.exception.UserAlreadyExistsException;
import com.alonso.salesapp.model.Role;
import com.alonso.salesapp.model.User;
import com.alonso.salesapp.repository.RoleRepo;
import com.alonso.salesapp.repository.UserRepo;
import com.alonso.salesapp.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private RoleRepo roleRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        role = Role.builder()
                .idRole(1)
                .name("ADMIN")
                .enabled(true)
                .build();

        user = User.builder()
                .idUser(1)
                .username("testuser")
                .password("encodedPassword")
                .role(role)
                .enabled(true)
                .build();
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfully() {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .build();

            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(user);
            when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(jwtUtil.generateAccessToken(user)).thenReturn("accessToken");
            when(jwtUtil.generateRefreshToken(user)).thenReturn("refreshToken");

            // When
            AuthResponse response = authService.login(request);

            // Then
            assertNotNull(response);
            assertEquals("accessToken", response.getAccessToken());
            assertEquals("refreshToken", response.getRefreshToken());
            assertEquals("Bearer", response.getTokenType());
            assertEquals(1, response.getIdUser());
            assertEquals("testuser", response.getUsername());
            assertEquals("ADMIN", response.getRole());

            verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepo, times(1)).findByUsername("testuser");
            verify(jwtUtil, times(1)).generateAccessToken(user);
            verify(jwtUtil, times(1)).generateRefreshToken(user);
        }

        @Test
        @DisplayName("Should throw BadCredentialsException with invalid credentials")
        void shouldThrowExceptionWithInvalidCredentials() {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .username("testuser")
                    .password("wrongpassword")
                    .build();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            // When & Then
            assertThrows(BadCredentialsException.class, () -> authService.login(request));

            verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepo, never()).findByUsername(anyString());
            verify(jwtUtil, never()).generateAccessToken(any());
        }

        @Test
        @DisplayName("Should throw ModelNotFoundException when user not found after authentication")
        void shouldThrowExceptionWhenUserNotFoundAfterAuth() {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .build();

            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(user);
            when(userRepo.findByUsername("testuser")).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ModelNotFoundException.class, () -> authService.login(request));

            verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepo, times(1)).findByUsername("testuser");
            verify(jwtUtil, never()).generateAccessToken(any());
        }
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully")
        void shouldRegisterSuccessfully() {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .username("newuser")
                    .password("password123")
                    .idRole(1)
                    .build();

            when(userRepo.findByUsername("newuser")).thenReturn(Optional.empty());
            when(roleRepo.findById(1)).thenReturn(Optional.of(role));
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepo.save(any(User.class))).thenReturn(user);
            when(jwtUtil.generateAccessToken(user)).thenReturn("accessToken");
            when(jwtUtil.generateRefreshToken(user)).thenReturn("refreshToken");

            // When
            AuthResponse response = authService.register(request);

            // Then
            assertNotNull(response);
            assertEquals("accessToken", response.getAccessToken());
            assertEquals("refreshToken", response.getRefreshToken());
            assertEquals("Bearer", response.getTokenType());
            assertEquals(1, response.getIdUser());
            assertEquals("testuser", response.getUsername());
            assertEquals("ADMIN", response.getRole());

            verify(userRepo, times(1)).findByUsername("newuser");
            verify(roleRepo, times(1)).findById(1);
            verify(passwordEncoder, times(1)).encode("password123");
            verify(userRepo, times(1)).save(any(User.class));
            verify(jwtUtil, times(1)).generateAccessToken(user);
            verify(jwtUtil, times(1)).generateRefreshToken(user);
        }

        @Test
        @DisplayName("Should throw UserAlreadyExistsException when username exists")
        void shouldThrowExceptionWhenUsernameExists() {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .username("existinguser")
                    .password("password123")
                    .idRole(1)
                    .build();

            when(userRepo.findByUsername("existinguser")).thenReturn(Optional.of(user));

            // When & Then
            UserAlreadyExistsException exception = assertThrows(
                    UserAlreadyExistsException.class,
                    () -> authService.register(request)
            );

            assertTrue(exception.getMessage().contains("Username already exists"));
            verify(userRepo, times(1)).findByUsername("existinguser");
            verify(roleRepo, never()).findById(any());
            verify(userRepo, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ModelNotFoundException when role not found")
        void shouldThrowExceptionWhenRoleNotFound() {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .username("newuser")
                    .password("password123")
                    .idRole(999)
                    .build();

            when(userRepo.findByUsername("newuser")).thenReturn(Optional.empty());
            when(roleRepo.findById(999)).thenReturn(Optional.empty());

            // When & Then
            ModelNotFoundException exception = assertThrows(
                    ModelNotFoundException.class,
                    () -> authService.register(request)
            );

            assertTrue(exception.getMessage().contains("Role not found"));
            verify(userRepo, times(1)).findByUsername("newuser");
            verify(roleRepo, times(1)).findById(999);
            verify(userRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("RefreshToken Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void shouldRefreshTokenSuccessfully() {
            // Given
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("validRefreshToken")
                    .build();

            when(jwtUtil.extractUsername("validRefreshToken")).thenReturn("testuser");
            when(userDetailsService.loadUserByUsername("testuser")).thenReturn(user);
            when(jwtUtil.validateToken("validRefreshToken", user)).thenReturn(true);
            when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(jwtUtil.generateAccessToken(user)).thenReturn("newAccessToken");
            when(jwtUtil.generateRefreshToken(user)).thenReturn("newRefreshToken");

            // When
            AuthResponse response = authService.refreshToken(request);

            // Then
            assertNotNull(response);
            assertEquals("newAccessToken", response.getAccessToken());
            assertEquals("newRefreshToken", response.getRefreshToken());
            assertEquals("Bearer", response.getTokenType());
            assertEquals(1, response.getIdUser());
            assertEquals("testuser", response.getUsername());
            assertEquals("ADMIN", response.getRole());

            verify(jwtUtil, times(1)).extractUsername("validRefreshToken");
            verify(userDetailsService, times(1)).loadUserByUsername("testuser");
            verify(jwtUtil, times(1)).validateToken("validRefreshToken", user);
            verify(userRepo, times(1)).findByUsername("testuser");
            verify(jwtUtil, times(1)).generateAccessToken(user);
            verify(jwtUtil, times(1)).generateRefreshToken(user);
        }

        @Test
        @DisplayName("Should throw InvalidTokenException when token is invalid")
        void shouldThrowExceptionWhenTokenIsInvalid() {
            // Given
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("invalidToken")
                    .build();

            when(jwtUtil.extractUsername("invalidToken")).thenReturn("testuser");
            when(userDetailsService.loadUserByUsername("testuser")).thenReturn(user);
            when(jwtUtil.validateToken("invalidToken", user)).thenReturn(false);

            // When & Then
            assertThrows(InvalidTokenException.class, () -> authService.refreshToken(request));

            verify(jwtUtil, times(1)).extractUsername("invalidToken");
            verify(userDetailsService, times(1)).loadUserByUsername("testuser");
            verify(jwtUtil, times(1)).validateToken("invalidToken", user);
            verify(userRepo, never()).findByUsername(anyString());
            verify(jwtUtil, never()).generateAccessToken(any());
        }

        @Test
        @DisplayName("Should throw ModelNotFoundException when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("validToken")
                    .build();

            when(jwtUtil.extractUsername("validToken")).thenReturn("testuser");
            when(userDetailsService.loadUserByUsername("testuser")).thenReturn(user);
            when(jwtUtil.validateToken("validToken", user)).thenReturn(true);
            when(userRepo.findByUsername("testuser")).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ModelNotFoundException.class, () -> authService.refreshToken(request));

            verify(jwtUtil, times(1)).extractUsername("validToken");
            verify(userDetailsService, times(1)).loadUserByUsername("testuser");
            verify(jwtUtil, times(1)).validateToken("validToken", user);
            verify(userRepo, times(1)).findByUsername("testuser");
            verify(jwtUtil, never()).generateAccessToken(any());
        }

        @Test
        @DisplayName("Should throw InvalidTokenException when token extraction fails")
        void shouldThrowExceptionWhenTokenExtractionFails() {
            // Given
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("malformedToken")
                    .build();

            when(jwtUtil.extractUsername("malformedToken"))
                    .thenThrow(new InvalidTokenException("Invalid token format"));

            // When & Then
            assertThrows(InvalidTokenException.class, () -> authService.refreshToken(request));

            verify(jwtUtil, times(1)).extractUsername("malformedToken");
            verify(userDetailsService, never()).loadUserByUsername(anyString());
            verify(jwtUtil, never()).generateAccessToken(any());
        }
    }
}


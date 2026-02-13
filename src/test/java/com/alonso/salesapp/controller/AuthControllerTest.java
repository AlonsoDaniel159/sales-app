package com.alonso.salesapp.controller;

import com.alonso.salesapp.dto.auth.AuthResponse;
import com.alonso.salesapp.dto.auth.LoginRequest;
import com.alonso.salesapp.dto.auth.RefreshTokenRequest;
import com.alonso.salesapp.dto.auth.RegisterRequest;
import com.alonso.salesapp.exception.InvalidTokenException;
import com.alonso.salesapp.exception.UserAlreadyExistsException;
import com.alonso.salesapp.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        authResponse = AuthResponse.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .tokenType("Bearer")
                .idUser(1)
                .username("testuser")
                .role("ADMIN")
                .build();
    }

    @Nested
    @DisplayName("Login Endpoint Tests")
    class LoginEndpointTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfully() throws Exception {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .build();

            when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("accessToken"))
                    .andExpect(jsonPath("$.refreshToken").value("refreshToken"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.idUser").value(1))
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));

            verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Should return 401 with invalid credentials")
        void shouldReturn401WithInvalidCredentials() throws Exception {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .username("testuser")
                    .password("wrongpassword")
                    .build();

            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BadCredentialsException("Invalid username or password"));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());

            verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when username is blank")
        void shouldReturn400WhenUsernameIsBlank() throws Exception {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .username("")
                    .password("password123")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).login(any(LoginRequest.class));
        }
    }

    @Nested
    @DisplayName("Register Endpoint Tests")
    class RegisterEndpointTests {

        @Test
        @DisplayName("Should register new user successfully")
        void shouldRegisterSuccessfully() throws Exception {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .username("newuser")
                    .password("password123")
                    .idRole(1)
                    .build();

            when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accessToken").value("accessToken"))
                    .andExpect(jsonPath("$.refreshToken").value("refreshToken"));

            verify(authService, times(1)).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("Should return 409 when username already exists")
        void shouldReturn409WhenUsernameExists() throws Exception {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .username("existinguser")
                    .password("password123")
                    .idRole(1)
                    .build();

            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new UserAlreadyExistsException("Username already exists"));

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());

            verify(authService, times(1)).register(any(RegisterRequest.class));
        }
    }

    @Nested
    @DisplayName("RefreshToken Endpoint Tests")
    class RefreshTokenEndpointTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void shouldRefreshTokenSuccessfully() throws Exception {
            // Given
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("validRefreshToken")
                    .build();

            when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(authResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/refresh")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("accessToken"));

            verify(authService, times(1)).refreshToken(any(RefreshTokenRequest.class));
        }

        @Test
        @DisplayName("Should return 401 with invalid refresh token")
        void shouldReturn401WithInvalidToken() throws Exception {
            // Given
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("invalidToken")
                    .build();

            when(authService.refreshToken(any(RefreshTokenRequest.class)))
                    .thenThrow(new InvalidTokenException("Invalid refresh token"));

            // When & Then
            mockMvc.perform(post("/api/auth/refresh")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());

            verify(authService, times(1)).refreshToken(any(RefreshTokenRequest.class));
        }
    }
}


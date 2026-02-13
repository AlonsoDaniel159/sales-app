package com.alonso.salesapp.controller;

import com.alonso.salesapp.dto.auth.AuthResponse;
import com.alonso.salesapp.dto.auth.LoginRequest;
import com.alonso.salesapp.dto.auth.RefreshTokenRequest;
import com.alonso.salesapp.dto.auth.RegisterRequest;
import com.alonso.salesapp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Endpoints para autenticación y gestión de tokens")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login de usuario", description = "Autentica un usuario y devuelve tokens JWT")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Registro de usuario", description = "Registra un nuevo usuario y devuelve tokens JWT")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Refrescar token", description = "Genera un nuevo access token y refresh token usando el refresh token actual")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}


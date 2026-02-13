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
import com.alonso.salesapp.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // Autenticar usuario
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            assert userDetails != null;
            User user = userRepo.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ModelNotFoundException("User not found"));

            // Generar tokens
            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            log.info("User '{}' logged in successfully", request.getUsername());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .idUser(user.getIdUser())
                    .username(user.getUsername())
                    .role(user.getRole().getName())
                    .build();

        } catch (BadCredentialsException e) {
            log.error("Invalid credentials for user: {}", request.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Verificar si el usuario ya existe
        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        // Buscar el rol
        Role role = roleRepo.findById(request.getIdRole())
                .orElseThrow(() -> new ModelNotFoundException("Role not found with id: " + request.getIdRole()));

        // Crear el nuevo usuario
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(true)
                .build();

        user = userRepo.save(user);

        // Generar tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("User '{}' registered successfully", request.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .idUser(user.getIdUser())
                .username(user.getUsername())
                .role(user.getRole().getName())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        try {
            // Extraer username del refresh token
            String username = jwtService.extractUsername(request.getRefreshToken());

            // Cargar usuario
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Validar el refresh token
            if (!jwtService.validateToken(request.getRefreshToken(), userDetails)) {
                throw new InvalidTokenException("Invalid refresh token");
            }

            User user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new ModelNotFoundException("User not found"));

            // Generar nuevos tokens
            String newAccessToken = jwtService.generateAccessToken(userDetails);
            String newRefreshToken = jwtService.generateRefreshToken(userDetails);

            log.info("Tokens refreshed for user: {}", username);

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .idUser(user.getIdUser())
                    .username(user.getUsername())
                    .role(user.getRole().getName())
                    .build();

        } catch (InvalidTokenException e) {
            log.error("Invalid refresh token: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            throw new InvalidTokenException("Error processing refresh token");
        }
    }
}


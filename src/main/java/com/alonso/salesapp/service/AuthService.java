package com.alonso.salesapp.service;

import com.alonso.salesapp.dto.auth.AuthResponse;
import com.alonso.salesapp.dto.auth.LoginRequest;
import com.alonso.salesapp.dto.auth.RefreshTokenRequest;
import com.alonso.salesapp.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
}


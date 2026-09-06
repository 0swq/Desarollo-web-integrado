package com.autopartes.service;

import com.autopartes.dto.auth.JwtAuthResponse;
import com.autopartes.dto.auth.LoginRequest;
import com.autopartes.dto.auth.RegisterRequest;

public interface AuthService {
    JwtAuthResponse login(LoginRequest loginRequest);
    JwtAuthResponse register(RegisterRequest registerRequest);
}

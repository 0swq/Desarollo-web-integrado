package com.autopartes.service.impl;

import com.autopartes.dto.auth.JwtAuthResponse;
import com.autopartes.dto.auth.LoginRequest;
import com.autopartes.dto.auth.RegisterRequest;
import com.autopartes.dto.auth.UserResponse;
import com.autopartes.exception.BusinessException;
import com.autopartes.model.Role;
import com.autopartes.model.User;
import com.autopartes.repository.UserRepository;
import com.autopartes.security.JwtTokenProvider;
import com.autopartes.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Override
    public JwtAuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        return JwtAuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .user(mapToUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public JwtAuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BusinessException("El email ya se encuentra registrado en el sistema");
        }

        Role role = registerRequest.getRol() != null ? registerRequest.getRol() : Role.ROLE_CLIENTE;

        User user = User.builder()
                .email(registerRequest.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .nombre(registerRequest.getNombre().trim())
                .apellido(registerRequest.getApellido().trim())
                .telefono(registerRequest.getTelefono())
                .direccion(registerRequest.getDireccion())
                .rol(role)
                .activo(true)
                .build();

        User savedUser = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerRequest.getEmail(), registerRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return JwtAuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .user(mapToUserResponse(savedUser))
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nombre(user.getNombre())
                .apellido(user.getApellido())
                .telefono(user.getTelefono())
                .direccion(user.getDireccion())
                .rol(user.getRol())
                .activo(user.getActivo())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

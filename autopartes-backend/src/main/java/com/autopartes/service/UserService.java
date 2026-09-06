package com.autopartes.service;

import com.autopartes.dto.auth.UserResponse;
import com.autopartes.model.Role;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    UserResponse updateUserRole(Long id, Role rol);
    UserResponse toggleUserStatus(Long id);
}

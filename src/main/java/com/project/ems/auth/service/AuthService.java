package com.project.ems.auth.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.auth.dto.AuthResponse;
import com.project.ems.auth.dto.RegisterRequest;
import com.project.ems.auth.repository.RoleRepository;
import com.project.ems.auth.repository.UserRepository;
import com.project.ems.auth.util.JwtUtil;
import com.project.ems.auth.util.PasswordUtil;
import com.project.ems.common.entity.Role;
import com.project.ems.common.entity.RoleName;
import com.project.ems.common.entity.User;
import com.project.ems.common.entity.User.UserStatus;
import com.project.ems.common.exception.InvalidCredentialsException;
import com.project.ems.common.exception.UnauthorizedException;
import com.project.ems.common.exception.UserAlreadyExistsException;
import com.project.ems.common.exception.UserNotFoundException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        String email = normalize(request.getEmail());

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        Role role = roleRepository.findByName(RoleName.ATTENDEE)
                .orElseThrow(() -> new IllegalStateException("Default role not configured"));

        User user = new User();
        user.setEmail(email);
        user.setPassword(PasswordUtil.hashPassword(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone().trim());
        user.setGender(request.getGender());
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(role);

        userRepository.save(user);

        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), role.getName().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return new AuthResponse(user.getId(), user.getEmail(), user.getFullName(),
                role.getName().name(), accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String email, String password) {

        String normalized = normalize(email);

        User user = userRepository.findByEmailIgnoreCase(normalized)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException("Account is not active");
        }

        String roleName = user.getRole().getName().name();
        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), roleName);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return new AuthResponse(user.getId(), user.getEmail(), user.getFullName(),
                roleName, accessToken, refreshToken);
    }

    public AuthResponse refreshAccessToken(String refreshToken) {

        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Provided token is not a refresh token");
        }

        Long userId = jwtUtil.extractUserId(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }

        String roleName = user.getRole().getName().name();
        String newAccessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), roleName);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        return new AuthResponse(user.getId(), user.getEmail(), user.getFullName(),
                roleName, newAccessToken, newRefreshToken);
    }

    private String normalize(String input) {
        return input.trim().toLowerCase();
    }
}

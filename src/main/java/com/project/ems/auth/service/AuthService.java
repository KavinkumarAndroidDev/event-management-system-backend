package com.project.ems.auth.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.auth.dto.AuthResponse;
import com.project.ems.auth.dto.RegisterRequest;
import com.project.ems.auth.repository.RevokedTokenRepository;
import com.project.ems.auth.repository.RoleRepository;
import com.project.ems.auth.repository.UserRepository;
import com.project.ems.auth.util.JwtUtil;
import com.project.ems.auth.util.PasswordUtil;
import com.project.ems.common.entity.RevokedToken;
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
    private final OtpService otpService;
    private final UserService userService;
    private final RevokedTokenRepository revokedTokenRepository;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       JwtUtil jwtUtil,
                       OtpService otpService,
                       UserService userService,
                       RevokedTokenRepository revokedTokenRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
        this.userService = userService;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    // -------------------------------------------------------------------------
    // Register
    // -------------------------------------------------------------------------

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        String email = normalize(request.getEmail());

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        Role role = roleRepository.findByName(RoleName.ATTENDEE)
                .orElseThrow(() -> new IllegalStateException("Default role ATTENDEE not configured in DB"));

        User user = new User();
        user.setEmail(email);
        user.setPassword(PasswordUtil.hashPassword(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setPhone(normalizePhone(request.getPhone()));
        user.setGender(request.getGender());
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(role);

        userRepository.save(user);

        String roleName     = role.getName().name();
        String accessToken  = jwtUtil.generateToken(user.getId(), user.getEmail(), roleName);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return new AuthResponse(user.getId(), user.getEmail(), user.getFullName(),
                roleName, accessToken, refreshToken);
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    @Transactional  
    public AuthResponse login(String email, String password) {

        String normalized = normalize(email);

        User user = userRepository.findByEmailIgnoreCase(normalized)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        // Always verify password before checking status — avoids leaking account existence
        if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException("Account is not active");
        }

        String roleName     = user.getRole().getName().name();
        String accessToken  = jwtUtil.generateToken(user.getId(), user.getEmail(), roleName);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return new AuthResponse(user.getId(), user.getEmail(), user.getFullName(),
                roleName, accessToken, refreshToken);
    }

    // -------------------------------------------------------------------------
    // Token Refresh
    // -------------------------------------------------------------------------

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

        String roleName        = user.getRole().getName().name();
        String newAccessToken  = jwtUtil.generateToken(user.getId(), user.getEmail(), roleName);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        return new AuthResponse(user.getId(), user.getEmail(), user.getFullName(),
                roleName, newAccessToken, newRefreshToken);
    }

    // -------------------------------------------------------------------------
    // OTP — verify OTP and return tokens (called from OtpController)
    // -------------------------------------------------------------------------

    @Transactional
    public AuthResponse verifyOtpAndLogin(String identifier, String otp) {

        // Verify OTP first — throws if invalid/expired
        otpService.verifyOtp(identifier, otp);

        User user = userService.findByIdentifier(identifier)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }

        String roleName     = user.getRole().getName().name();
        String accessToken  = jwtUtil.generateToken(user.getId(), user.getEmail(), roleName);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return new AuthResponse(user.getId(), user.getEmail(), user.getFullName(),
                roleName, accessToken, refreshToken);
    }

    // -------------------------------------------------------------------------
    // Password Reset
    // -------------------------------------------------------------------------
    
    @Transactional
    public void resetPassword(String identifier, String otp, String newPassword) {
        // Verify OTP first — throws if invalid/expired
        otpService.verifyOtp(identifier, otp);
        // Reset password after successful OTP check
        userService.resetPassword(identifier, newPassword);
    }

    // -------------------------------------------------------------------------
    // Logout
    // -------------------------------------------------------------------------
    
    @Transactional
    public void logout(String token) {
        revokedTokenRepository.save(new RevokedToken(token));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String normalize(String input) {
        return input.trim().toLowerCase();
    }

    private String normalizePhone(String phone) {
        return phone == null ? null : phone.trim().replaceAll("[\\s\\-]", "");
    }
}
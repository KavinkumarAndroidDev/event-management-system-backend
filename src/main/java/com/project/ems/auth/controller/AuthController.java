package com.project.ems.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.auth.dto.AuthResponse;
import com.project.ems.auth.dto.LoginRequest;
import com.project.ems.auth.dto.RefreshTokenRequest;
import com.project.ems.auth.dto.RegisterRequest;
import com.project.ems.auth.dto.ResetPasswordRequest;
import com.project.ems.auth.dto.SendOtpRequest;
import com.project.ems.auth.dto.UpdateUserRequest;
import com.project.ems.auth.dto.VerifyOtpRequest;
import com.project.ems.auth.service.AuthService;
import com.project.ems.auth.service.OtpService;
import com.project.ems.auth.service.UserService;
import com.project.ems.auth.util.JwtUtil;
import com.project.ems.common.entity.User;
import com.project.ems.common.exception.UserNotFoundException;

import jakarta.validation.Valid;

@RestController
@RequestMapping
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService,
                          UserService userService,
                          OtpService otpService,
                          JwtUtil jwtUtil) {
        this.authService = authService;
        this.userService = userService;
        this.otpService = otpService;
        this.jwtUtil = jwtUtil;
    }

    // POST /auth/register — PUBLIC
    @PostMapping("/auth/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /auth/login — PUBLIC
    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    // POST /auth/logout — any authenticated (stateless — client discards token)
    @PostMapping("/auth/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }

    // POST /auth/refresh — PUBLIC (uses refresh token)
    @PostMapping("/auth/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    // POST /auth/send-otp — PUBLIC
    @PostMapping("/auth/send-otp")
    public ResponseEntity<String> sendOtp(@Valid @RequestBody SendOtpRequest request) {

        userService.findByIdentifier(request.getIdentifier())
                .orElseThrow(() -> new UserNotFoundException("User not registered"));

        otpService.sendOtp(request.getIdentifier());
        return ResponseEntity.ok("OTP sent");
    }

    // POST /auth/verify-otp — PUBLIC (verifies OTP and returns JWT tokens)
    @PostMapping("/auth/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {

        otpService.verifyOtp(request.getIdentifier(), request.getOtp());

        User user = userService.findByIdentifier(request.getIdentifier())
                .orElseThrow(() -> new UserNotFoundException("User not registered"));

        String roleName = user.getRole().getName().name();
        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), roleName);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getEmail(),
                user.getFullName(), roleName, accessToken, refreshToken));
    }

    // POST /auth/reset-password — PUBLIC (OTP verified inside this call)
    @PostMapping("/auth/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {

        otpService.verifyOtp(request.getIdentifier(), request.getOtp());
        userService.resetPassword(request.getIdentifier(), request.getNewPassword());

        return ResponseEntity.ok("Password reset successful");
    }

    // GET /users/me — any authenticated user
    @GetMapping("/users/me")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthResponse> getMe(@AuthenticationPrincipal Long userId) {

        User user = userService.getUserOrThrow(userId);
        String roleName = user.getRole().getName().name();

        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getEmail(),
                user.getFullName(), roleName, null, null));
    }

    // PUT /users/me — any authenticated user
    @PutMapping("/users/me")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthResponse> updateMe(@Valid @RequestBody UpdateUserRequest request,
                                                 @AuthenticationPrincipal Long userId) {

        User user = userService.updateUser(userId, request);
        String roleName = user.getRole().getName().name();

        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getEmail(),
                user.getFullName(), roleName, null, null));
    }

    // DELETE /users/me — any authenticated user
    @DeleteMapping("/users/me")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteMe(@AuthenticationPrincipal Long userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.ok("Account deactivated");
    }
}

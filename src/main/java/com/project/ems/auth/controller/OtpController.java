package com.project.ems.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.auth.dto.AuthResponse;
import com.project.ems.auth.dto.ResetPasswordRequest;
import com.project.ems.auth.dto.SendOtpRequest;
import com.project.ems.auth.dto.VerifyOtpRequest;
import com.project.ems.auth.service.AuthService;
import com.project.ems.auth.service.OtpService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class OtpController {

    private final OtpService otpService;
    private final AuthService authService;

    public OtpController(OtpService otpService, AuthService authService) {
        this.otpService = otpService;
        this.authService = authService;
    }

    // POST /auth/send-otp
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        otpService.sendOtpToRegisteredUser(request.getIdentifier());
        return ResponseEntity.ok("OTP sent");
    }

    // POST /auth/verify-otp — verifies OTP and returns a full auth response with tokens
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtpAndLogin(request.getIdentifier(), request.getOtp());
        return ResponseEntity.ok(response);
    }

    // POST /auth/reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getIdentifier(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successful");
    }
}
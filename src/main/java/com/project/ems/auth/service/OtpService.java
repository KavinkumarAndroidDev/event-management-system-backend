package com.project.ems.auth.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.auth.repository.OtpRepository;
import com.project.ems.common.entity.OtpVerification;
import com.project.ems.common.exception.InvalidOtpException;
import com.project.ems.common.exception.OtpAlreadySentException;
import com.project.ems.common.exception.OtpExpiredException;
import com.project.ems.common.exception.OtpNotFoundException;
import com.project.ems.common.exception.UserNotFoundException;

@Service
public class OtpService {


    private final OtpRepository otpRepository;
    private final UserService userService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${otp.expiry.minutes:2}")
    private int otpExpiryMinutes;

    @Value("${otp.expiry.seconds:30}")
    private int otpExpirySeconds;

    public OtpService(OtpRepository otpRepository, UserService userService) {
        this.otpRepository = otpRepository;
        this.userService = userService;
    }

    // -------------------------------------------------------------------------
    // Generate
    // -------------------------------------------------------------------------

    public String generateOtp() {
        int otp = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(otp);
    }

    @Transactional
    public void sendOtpToRegisteredUser(String identifier) {
        userService.findByIdentifier(identifier)
                .orElseThrow(() -> new UserNotFoundException("User not registered"));
        sendOtp(identifier);
    }

    @Transactional
    public void sendOtp(String identifier) {
        String normalized = normalize(identifier);

        Optional<OtpVerification> existingOtp =
                otpRepository.findTopByIdentifierAndUsedFalseOrderByCreatedAtDesc(normalized);

        // Block resend if a valid OTP already exists
        if (existingOtp.isPresent() &&
                existingOtp.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new OtpAlreadySentException("OTP already sent. Please wait before requesting a new one.");
        }

        String otp = generateOtp();
        System.out.println("OTP FOR "+identifier +" :" +otp);
        OtpVerification entity = new OtpVerification();
        entity.setIdentifier(normalized);
        entity.setOtpCode(otp);
        entity.setExpiresAt(LocalDateTime.now()
                .plusMinutes(otpExpiryMinutes)
                .plusSeconds(otpExpirySeconds));
        
        otpRepository.save(entity);
    }

    // -------------------------------------------------------------------------
    // Verify OTP
    // -------------------------------------------------------------------------

    @Transactional
    public void verifyOtp(String identifier, String otp) {
        String normalized = normalize(identifier);

        OtpVerification record = otpRepository
                .findTopByIdentifierAndUsedFalseOrderByCreatedAtDesc(normalized)
                .orElseThrow(() -> new OtpNotFoundException("No active OTP found"));

        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OtpExpiredException("OTP has expired");
        }

        if (!record.getOtpCode().equals(otp)) {
            throw new InvalidOtpException("Invalid OTP");
        }

        record.setUsed(true);
        otpRepository.save(record);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String normalize(String identifier) {
        return identifier.trim().toLowerCase();
    }
}
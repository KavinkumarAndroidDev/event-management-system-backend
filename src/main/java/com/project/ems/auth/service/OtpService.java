package com.project.ems.auth.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.auth.repository.OtpRepository;
import com.project.ems.common.entity.OtpVerification;
import com.project.ems.common.entity.User;
import com.project.ems.common.exception.InvalidOtpException;
import com.project.ems.common.exception.OtpAlreadySentException;
import com.project.ems.common.exception.OtpExpiredException;
import com.project.ems.common.exception.OtpNotFoundException;
import com.project.ems.common.exception.UserNotFoundException;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private final OtpRepository otpRepository;
    private final UserService userService;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${otp.expiry.minutes:2}")
    private int otpExpiryMinutes;

    @Value("${otp.expiry.seconds:30}")
    private int otpExpirySeconds;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public OtpService(OtpRepository otpRepository, UserService userService, JavaMailSender mailSender) {
        this.otpRepository = otpRepository;
        this.userService = userService;
        this.mailSender = mailSender;
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
        User user = userService.findByIdentifier(identifier)
                .orElseThrow(() -> new UserNotFoundException("User not registered"));
        sendOtp(identifier, user.getEmail());
    }

    @Transactional
    public void sendOtp(String identifier) {
        sendOtp(identifier, identifier);
    }

    private void sendOtp(String identifier, String email) {
        String normalized = normalize(identifier);

        Optional<OtpVerification> existingOtp =
                otpRepository.findTopByIdentifierAndUsedFalseOrderByCreatedAtDesc(normalized);

        // Block resend if a valid OTP already exists
        if (existingOtp.isPresent() &&
                existingOtp.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new OtpAlreadySentException("OTP already sent. Please wait before requesting a new one.");
        }

        String otp = generateOtp();
        sendOtpEmail(email, otp);

        OtpVerification entity = new OtpVerification();
        entity.setIdentifier(normalized);
        entity.setOtpCode(otp);
        entity.setExpiresAt(LocalDateTime.now()
                .plusMinutes(otpExpiryMinutes)
                .plusSeconds(otpExpirySeconds));
        
        otpRepository.save(entity);
    }

    private void sendOtpEmail(String email, String otp) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required to send OTP");
        }

        if (mailUsername == null || mailUsername.isBlank()) {
            logger.info("Mail username not configured. Dev OTP for {} is {}", email, otp);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your SyncEvent OTP Code");
        message.setText("Your OTP is: " + otp + "\n\nThis code is valid for only 150 seconds. Do not share it with anyone.");
        mailSender.send(message);
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

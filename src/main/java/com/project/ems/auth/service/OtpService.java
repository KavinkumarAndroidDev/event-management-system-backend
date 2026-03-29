package com.project.ems.auth.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.auth.repository.OtpRepository;
import com.project.ems.common.entity.OtpVerification;
import com.project.ems.common.exception.InvalidOtpException;
import com.project.ems.common.exception.OtpExpiredException;
import com.project.ems.common.exception.OtpAlreadySentException;
import com.project.ems.common.exception.OtpNotFoundException;

@Service
public class OtpService {

    private final OtpRepository otpRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    public String generateOtp() {
        int otp = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(otp);
    }

    @Transactional
    public void sendOtp(String identifier) {

        String normalized = normalize(identifier);

        Optional<OtpVerification> existingOtp =
                otpRepository.findTopByIdentifierAndUsedFalseOrderByCreatedAtDesc(normalized);

        if (existingOtp.isPresent() &&
            existingOtp.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new OtpAlreadySentException("OTP already sent. Please wait before requesting a new one.");
        }

        String otp = generateOtp();

        OtpVerification entity = new OtpVerification();
        entity.setIdentifier(normalized);
        entity.setOtpCode(otp);
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(2).plusSeconds(30));

        otpRepository.save(entity);

        System.out.println("OTP for " + normalized + " = " + otp);
    }

    @Transactional
    public void verifyOtp(String identifier, String otp) {

        String normalized = normalize(identifier);

        OtpVerification record = otpRepository
                .findTopByIdentifierAndUsedFalseOrderByCreatedAtDesc(normalized)
                .orElseThrow(() -> new OtpNotFoundException("OTP not found"));

        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OtpExpiredException("OTP expired");
        }

        if (!record.getOtpCode().equals(otp)) {
            throw new InvalidOtpException("Invalid OTP");
        }

        record.setUsed(true);
        otpRepository.save(record);
    }

    private String normalize(String identifier) {
        return identifier.trim().toLowerCase();
    }
}
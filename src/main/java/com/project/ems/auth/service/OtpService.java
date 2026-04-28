package com.project.ems.auth.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

import jakarta.mail.internet.MimeMessage;

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

		Optional<OtpVerification> existingOtp = otpRepository
				.findTopByIdentifierAndUsedFalseOrderByCreatedAtDesc(normalized);

		// Block resend if a valid OTP already exists
		if (existingOtp.isPresent() && existingOtp.get().getExpiresAt().isAfter(LocalDateTime.now())) {
			throw new OtpAlreadySentException("OTP already sent. Please wait before requesting a new one.");
		}

		String otp = generateOtp();
		sendOtpEmail(email, otp);

		OtpVerification entity = new OtpVerification();
		entity.setIdentifier(normalized);
		entity.setOtpCode(otp);
		entity.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes).plusSeconds(otpExpirySeconds));

		otpRepository.save(entity);
	}

//    private void sendOtpEmail(String email, String otp) {
//        if (email == null || email.isBlank() || !email.contains("@")) {
//            throw new IllegalArgumentException("Valid email is required to send OTP");
//        }
//
//        if (mailUsername == null || mailUsername.isBlank()) {
//            logger.info("Mail username not configured. Dev OTP for {} is {}", email, otp);
//            return;
//        }
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(email);
//        message.setSubject("Your SyncEvent OTP Code");
//        message.setText("Your OTP is: " + otp + "\n\nThis code is valid for only 150 seconds. Do not share it with anyone.");
//        mailSender.send(message);
//    }
	private void sendOtpEmail(String email, String otp) {
		if (email == null || email.isBlank() || !email.contains("@")) {
			throw new IllegalArgumentException("Valid email is required to send OTP");
		}

		if (mailUsername == null || mailUsername.isBlank()) {
			logger.info("Mail username not configured. Dev OTP for {} is {}", email, otp);
			return;
		}

		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setTo(email);
			helper.setSubject("Your SyncEvent OTP Code");

			String htmlContent = """
					<!DOCTYPE html>
					<html>
					<head>
					<meta name="viewport" content="width=device-width, initial-scale=1.0">
					<style>
					@media (prefers-color-scheme: dark) {
					  .bg-body { background:#111827 !important; }
					  .card { background:#1F2937 !important; border-color:#374151 !important; }
					  .text-main { color:#F9FAFB !important; }
					  .text-sub { color:#D1D5DB !important; }
					  .otp-box { background:#111827 !important; border-color:#374151 !important; color:#F9FAFB !important; }
					}
					</style>
					</head>

					<body style="margin:0; padding:0;">

					<table width="100%" cellpadding="0" cellspacing="0" border="0" class="bg-body" style="background:#F9FAFB; padding:20px 10px;">
					<tr>
					<td align="center">

					  <!-- Card -->
					  <table cellpadding="0" cellspacing="0" border="0" class="card"
					         style="width:100%; max-width:420px; background:#ffffff; border-radius:12px; border:1px solid #E5E7EB;">

					    <tr>
					      <td style="padding:28px 20px; font-family:Arial, sans-serif; text-align:center;">

					        <!-- Brand -->
					        <div class="text-main" style="font-size:20px; font-weight:700; color:#111827; margin-bottom:18px;">
					          SyncEvent
					        </div>

					        <!-- Title -->
					        <div class="text-main" style="font-size:18px; font-weight:700; color:#111827; margin-bottom:8px;">
					          Verify your login
					        </div>

					        <!-- Message -->
					        <div class="text-sub" style="font-size:14px; color:#4B5563; line-height:1.6; margin-bottom:22px;">
					          Enter the verification code below to continue signing in.
					        </div>

					        <!-- OTP -->
					        <table align="center" cellpadding="0" cellspacing="0" class="otp-box"
					               style="border:1px solid #E5E7EB; border-radius:8px; background:#F9FAFB; margin-bottom:20px;">
					          <tr>
					            <td style="font-size:24px; font-weight:700; letter-spacing:5px; padding:12px 18px; color:#111827;">
					              {{OTP}}
					            </td>
					          </tr>
					        </table>

					        <!-- Expiry -->
					        <div class="text-sub" style="font-size:13px; color:#9CA3AF; margin-bottom:6px;">
					          This code expires in <b>150 seconds</b>
					        </div>

					        <!-- Warning -->
					        <div class="text-sub" style="font-size:12px; color:#9CA3AF;">
					          Never share this code with anyone
					        </div>

					      </td>
					    </tr>
					  </table>

					  <!-- Footer -->
					  <table width="100%" cellpadding="0" cellspacing="0" style="max-width:420px;">
					    <tr>
					      <td align="center" style="font-size:12px; color:#9CA3AF; font-family:Arial, sans-serif; padding-top:14px;">
					        If you didn’t request this, you can safely ignore this email.
					      </td>
					    </tr>
					  </table>

					</td>
					</tr>
					</table>

					</body>
					</html>
					""";

			htmlContent = htmlContent.replace("{{OTP}}", otp);
			helper.setText(htmlContent, true);

			mailSender.send(mimeMessage);

		} catch (Exception e) {
			logger.error("Failed to send OTP email to {}", email, e);
			throw new RuntimeException("Failed to send OTP email", e);
		}
	}

	// -------------------------------------------------------------------------
	// Verify OTP
	// -------------------------------------------------------------------------

	@Transactional
	public void verifyOtp(String identifier, String otp) {
		String normalized = normalize(identifier);

		OtpVerification record = otpRepository.findTopByIdentifierAndUsedFalseOrderByCreatedAtDesc(normalized)
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

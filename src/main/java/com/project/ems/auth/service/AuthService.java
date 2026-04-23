package com.project.ems.auth.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.auth.dto.AuthResponse;
import com.project.ems.auth.dto.RegisterRequest;
import com.project.ems.auth.dto.RegisterResponse;
import com.project.ems.auth.repository.RevokedTokenRepository;
import com.project.ems.auth.repository.RoleRepository;
import com.project.ems.auth.repository.UserRepository;
import com.project.ems.auth.util.JwtUtil;
import com.project.ems.auth.util.PasswordUtil;
import com.project.ems.common.entity.OrganizerProfile;
import com.project.ems.common.entity.RevokedToken;
import com.project.ems.common.entity.Role;
import com.project.ems.common.entity.RoleName;
import com.project.ems.common.entity.User;
import com.project.ems.common.entity.User.UserStatus;
import com.project.ems.common.exception.InvalidCredentialsException;
import com.project.ems.common.exception.UnauthorizedException;
import com.project.ems.common.exception.UserAlreadyExistsException;
import com.project.ems.common.exception.UserNotFoundException;
import com.project.ems.organizer.dto.OrganizerRegisterRequest;
import com.project.ems.organizer.repository.OrganizerProfileRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final UserService userService;
    private final RevokedTokenRepository revokedTokenRepository;
    private final OrganizerProfileRepository organizerProfileRepository;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       JwtUtil jwtUtil,
                       OtpService otpService,
                       UserService userService,
                       RevokedTokenRepository revokedTokenRepository,
                       OrganizerProfileRepository organizerProfileRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
        this.userService = userService;
        this.revokedTokenRepository = revokedTokenRepository;
        this.organizerProfileRepository = organizerProfileRepository;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String email = normalize(request.getEmail());

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }
        if(request.getPhone() != null ) {
        	if (userRepository.findByPhoneIgnoreCase(request.getPhone()).isPresent()) {
                throw new UserAlreadyExistsException("User already exists");
            }
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

        return new RegisterResponse(user.getId(), user.getEmail(), user.getFullName(), role.getName().name());
    }

    @Transactional
    public RegisterResponse registerOrganizer(OrganizerRegisterRequest request) {

        String email = normalize(request.getEmail());

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        if(request.getPhone() != null ) {
        	if (userRepository.findByPhoneIgnoreCase(request.getPhone()).isPresent()) {
                throw new UserAlreadyExistsException("User already exists");
            }
        }
        Role organizerRole = roleRepository.findByName(RoleName.ORGANIZER)
                .orElseThrow(() -> new IllegalStateException("ORGANIZER role not configured in DB"));

        User user = new User();
        user.setEmail(email);
        user.setPassword(PasswordUtil.hashPassword(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setPhone(normalizePhone(request.getPhone()));
        user.setGender(request.getGender());
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(organizerRole);

        userRepository.save(user);

        OrganizerProfile profile = new OrganizerProfile();
        profile.setUser(user);
        profile.setOrganizationName(request.getOrganizationName().trim());

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            profile.setDescription(request.getDescription().trim());
        }
        if (request.getWebsite() != null && !request.getWebsite().isBlank()) {
            profile.setWebsite(request.getWebsite().trim());
        }
        if (request.getInstagram() != null && !request.getInstagram().isBlank()) {
            profile.setInstagram(request.getInstagram().trim());
        }
        if (request.getLinkedin() != null && !request.getLinkedin().isBlank()) {
            profile.setLinkedin(request.getLinkedin().trim());
        }

        profile.setVerified(false);
        profile.setCreatedAt(LocalDateTime.now());

        organizerProfileRepository.save(profile);

        return new RegisterResponse(user.getId(), user.getEmail(), user.getFullName(), organizerRole.getName().name());
    }

    @Transactional
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

        String roleName     = user.getRole().getName().name();
        String accessToken  = jwtUtil.generateToken(user.getId(), user.getEmail(), roleName);
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

        String roleName        = user.getRole().getName().name();
        String newAccessToken  = jwtUtil.generateToken(user.getId(), user.getEmail(), roleName);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        return new AuthResponse(user.getId(), user.getEmail(), user.getFullName(),
                roleName, newAccessToken, newRefreshToken);
    }

    @Transactional
    public AuthResponse verifyOtpAndLogin(String identifier, String otp) {

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

    @Transactional
    public void resetPassword(String identifier, String otp, String newPassword) {
        otpService.verifyOtp(identifier, otp);
        userService.resetPassword(identifier, newPassword);
    }

    @Transactional
    public void logout(String token) {
        revokedTokenRepository.save(new RevokedToken(token));
    }

    private String normalize(String input) {
        return input.trim().toLowerCase();
    }

    private String normalizePhone(String phone) {
        return phone == null ? null : phone.trim().replaceAll("[\\s\\-]", "");
    }
}

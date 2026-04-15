package com.project.ems.auth.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.auth.dto.UpdateUserRequest;
import com.project.ems.auth.repository.UserRepository;
import com.project.ems.auth.util.PasswordUtil;
import com.project.ems.common.entity.User;
import com.project.ems.common.exception.UserNotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public Optional<User> findByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Identifier cannot be empty");
        }

        String trimmed = identifier.trim();

        if (trimmed.contains("@")) {
            return userRepository.findByEmailIgnoreCase(trimmed);
        }

        String normalizedPhone = trimmed.replaceAll("[^\\d]", "");
        return userRepository.findByPhone(normalizedPhone);
    }

    @Transactional
    public User updateUser(Long userId, UpdateUserRequest request) {
        User user = getUserOrThrow(userId);

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            String normalizedPhone = request.getPhone().trim().replaceAll("[^\\d]", "");
            user.setPhone(normalizedPhone);
        }

        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(PasswordUtil.hashPassword(request.getPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void resetPassword(String identifier, String newPassword) {
        User user = findByIdentifier(identifier)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setPassword(PasswordUtil.hashPassword(newPassword));
        userRepository.save(user);
    }

    public com.project.ems.auth.dto.UserProfileResponse toProfileResponse(User user) {
        return new com.project.ems.auth.dto.UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getGender(),
                user.getRole().getName().name(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

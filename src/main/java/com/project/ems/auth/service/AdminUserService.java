package com.project.ems.auth.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.auth.dto.UserListDTO;
import com.project.ems.auth.dto.UserStatusRequest;
import com.project.ems.auth.repository.UserRepository;
import com.project.ems.common.entity.RoleName;
import com.project.ems.common.entity.User;
import com.project.ems.common.exception.UserNotFoundException;

@Service
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserListDTO> getAllUsers(
            String search,
            User.UserStatus status,
            String role,
            int page,
            int size,
            String sort
    ) {
        String[] parts = sort.split(",");

        String property = parts[0];
        String direction = (parts.length > 1) ? parts[1] : "asc";

        Sort.Direction dir = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, property));

        String normalizedSearch = (search != null && !search.isBlank()) ? search.trim() : null;

        RoleName roleName = null;
        if (role != null && !role.isBlank()) {
            roleName = RoleName.valueOf(role.trim().toUpperCase());
        }

        return userRepository.findByFilters(normalizedSearch, status, roleName, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public UserListDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toDTO(user);
    }

    @Transactional
    public UserListDTO updateUserStatus(Long id, UserStatusRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setStatus(request.getStatus());
        return toDTO(userRepository.save(user));
    }

    private UserListDTO toDTO(User user) {
        String roleName = user.getRole() != null ? user.getRole().getName().name() : null;

        return new UserListDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getGender(),
                roleName,
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}

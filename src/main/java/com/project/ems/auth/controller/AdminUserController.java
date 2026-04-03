package com.project.ems.auth.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.auth.dto.UserListDTO;
import com.project.ems.auth.dto.UserStatusRequest;
import com.project.ems.auth.service.AdminUserService;
import com.project.ems.common.entity.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<Page<UserListDTO>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) User.UserStatus status,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Page<UserListDTO> users = adminUserService.getAllUsers(search, status, role, page, size, sort);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserListDTO> getUserById(@PathVariable Long id) {
        UserListDTO user = adminUserService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserListDTO> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest request
    ) {
        UserListDTO user = adminUserService.updateUserStatus(id, request);
        return ResponseEntity.ok(user);
    }
}

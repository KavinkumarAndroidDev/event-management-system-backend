package com.project.ems.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.auth.dto.AuthResponse;
import com.project.ems.auth.dto.UpdateUserRequest;
import com.project.ems.auth.service.UserService;
import com.project.ems.common.entity.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthResponse> getMe(@AuthenticationPrincipal Long userId) {
        User user = userService.getUserOrThrow(userId);
        String roleName = user.getRole().getName().name();
        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getEmail(), user.getFullName(), roleName, null, null));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthResponse> updateMe(
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        User user = userService.updateUser(userId, request);
        String roleName = user.getRole().getName().name();
        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getEmail(), user.getFullName(), roleName, null, null));
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteMe(@AuthenticationPrincipal Long userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.ok("Account deactivated");
    }
}

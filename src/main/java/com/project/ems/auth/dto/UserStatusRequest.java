package com.project.ems.auth.dto;

import com.project.ems.common.entity.User;
import jakarta.validation.constraints.NotNull;

public class UserStatusRequest {

    @NotNull(message = "Status is required")
    private User.UserStatus status;

    public User.UserStatus getStatus() {
        return status;
    }

    public void setStatus(User.UserStatus status) {
        this.status = status;
    }
}

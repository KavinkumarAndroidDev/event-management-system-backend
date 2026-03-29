package com.project.ems.category.dto;

import com.project.ems.common.entity.Status;
import jakarta.validation.constraints.NotNull;

public class CategoryStatusRequest {

    @NotNull(message = "Status is required")
    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}

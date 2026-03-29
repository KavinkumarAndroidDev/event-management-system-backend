package com.project.ems.venue.dto;

import com.project.ems.common.entity.Status;
import jakarta.validation.constraints.NotNull;

public class VenueStatusRequest {

    @NotNull(message = "Status is required")
    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}

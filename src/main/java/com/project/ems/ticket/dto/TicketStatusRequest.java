package com.project.ems.ticket.dto;

import jakarta.validation.constraints.NotBlank;

public class TicketStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

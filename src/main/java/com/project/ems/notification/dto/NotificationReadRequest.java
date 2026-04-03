package com.project.ems.notification.dto;

import jakarta.validation.constraints.NotNull;

public class NotificationReadRequest {

    @NotNull
    private Boolean isRead;

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
}

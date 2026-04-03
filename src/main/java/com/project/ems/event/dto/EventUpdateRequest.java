package com.project.ems.event.dto;

import java.time.LocalDateTime;

public class EventUpdateRequest {

    private String title;
    private String description;
    private String fullDescription;
    private Long categoryId;
    private Long venueId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime cancellationDeadline;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFullDescription() { return fullDescription; }
    public void setFullDescription(String fullDescription) { this.fullDescription = fullDescription; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getVenueId() { return venueId; }
    public void setVenueId(Long venueId) { this.venueId = venueId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public LocalDateTime getCancellationDeadline() { return cancellationDeadline; }
    public void setCancellationDeadline(LocalDateTime cancellationDeadline) { this.cancellationDeadline = cancellationDeadline; }
}

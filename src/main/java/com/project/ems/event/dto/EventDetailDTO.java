package com.project.ems.event.dto;

import java.time.LocalDateTime;

public class EventDetailDTO {

    private Long id;
    private String title;
    private String status;

    private String description;
    private String fullDescription;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Boolean isCancellable;
    private LocalDateTime cancellationDeadline;

    private CategoryDTO category;
    private VenueDTO venue;
    private OrganizerDTO organizer;

    // Flattened convenience fields
    private String categoryName;
    private String venueName;
    private String organizerName;
    private Integer capacity;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFullDescription() {
		return fullDescription;
	}

	public void setFullDescription(String fullDescription) {
		this.fullDescription = fullDescription;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public Boolean getIsCancellable() {
		return isCancellable;
	}

	public void setIsCancellable(Boolean isCancellable) {
		this.isCancellable = isCancellable;
	}

	public LocalDateTime getCancellationDeadline() {
		return cancellationDeadline;
	}

	public void setCancellationDeadline(LocalDateTime cancellationDeadline) {
		this.cancellationDeadline = cancellationDeadline;
	}

	public CategoryDTO getCategory() {
		return category;
	}

	public void setCategory(CategoryDTO category) {
		this.category = category;
	}

	public VenueDTO getVenue() {
		return venue;
	}

	public void setVenue(VenueDTO venue) {
		this.venue = venue;
	}

	public OrganizerDTO getOrganizer() {
		return organizer;
	}

	public void setOrganizer(OrganizerDTO organizer) {
		this.organizer = organizer;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getVenueName() {
		return venueName;
	}

	public void setVenueName(String venueName) {
		this.venueName = venueName;
	}

	public String getOrganizerName() {
		return organizerName;
	}

	public void setOrganizerName(String organizerName) {
		this.organizerName = organizerName;
	}
    
}

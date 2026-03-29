package com.project.ems.common.entity;


import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String fullDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    // Organizer = User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    // Audit users
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by")
    private User cancelledBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
    
    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets;

    private LocalDateTime approvedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime publishedAt;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Boolean isCancellable;
    private LocalDateTime cancellationDeadline;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum EventStatus {
        DRAFT,
        PENDING_APPROVAL,
        APPROVED,
        PUBLISHED,
        CANCELLED,
        COMPLETED
    }
    
    
    public Event() {
    	
    }


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


	public Category getCategory() {
		return category;
	}


	public void setCategory(Category category) {
		this.category = category;
	}


	public Venue getVenue() {
		return venue;
	}


	public void setVenue(Venue venue) {
		this.venue = venue;
	}


	public User getOrganizer() {
		return organizer;
	}


	public void setOrganizer(User organizer) {
		this.organizer = organizer;
	}
	
	


	public List<Ticket> getTickets() {
		return tickets;
	}


	public void setTickets(List<Ticket> tickets) {
		this.tickets = tickets;
	}


	public EventStatus getStatus() {
		return status;
	}


	public void setStatus(EventStatus status) {
		this.status = status;
	}


	public User getApprovedBy() {
		return approvedBy;
	}


	public void setApprovedBy(User approvedBy) {
		this.approvedBy = approvedBy;
	}


	public User getCancelledBy() {
		return cancelledBy;
	}


	public void setCancelledBy(User cancelledBy) {
		this.cancelledBy = cancelledBy;
	}


	public User getCreatedBy() {
		return createdBy;
	}


	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}


	public User getUpdatedBy() {
		return updatedBy;
	}


	public void setUpdatedBy(User updatedBy) {
		this.updatedBy = updatedBy;
	}


	public LocalDateTime getApprovedAt() {
		return approvedAt;
	}


	public void setApprovedAt(LocalDateTime approvedAt) {
		this.approvedAt = approvedAt;
	}


	public LocalDateTime getCancelledAt() {
		return cancelledAt;
	}


	public void setCancelledAt(LocalDateTime cancelledAt) {
		this.cancelledAt = cancelledAt;
	}


	public LocalDateTime getPublishedAt() {
		return publishedAt;
	}


	public void setPublishedAt(LocalDateTime publishedAt) {
		this.publishedAt = publishedAt;
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


	public LocalDateTime getCreatedAt() {
		return createdAt;
	}


	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}


	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}


	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
    
    
}
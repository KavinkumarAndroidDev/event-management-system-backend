package com.project.ems.report.dto;

import java.math.BigDecimal;

public class SummaryResponse {

    private Long totalEvents;
    private Long publishedEvents;
    private Long totalRegistrations;
    private Long confirmedRegistrations;
    private BigDecimal totalRevenue;
    private Long totalParticipants;
    private Long checkedInParticipants;
    private Double averageRating;

    public Long getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(Long totalEvents) {
        this.totalEvents = totalEvents;
    }

    public Long getPublishedEvents() {
        return publishedEvents;
    }

    public void setPublishedEvents(Long publishedEvents) {
        this.publishedEvents = publishedEvents;
    }

    public Long getTotalRegistrations() {
        return totalRegistrations;
    }

    public void setTotalRegistrations(Long totalRegistrations) {
        this.totalRegistrations = totalRegistrations;
    }

    public Long getConfirmedRegistrations() {
        return confirmedRegistrations;
    }

    public void setConfirmedRegistrations(Long confirmedRegistrations) {
        this.confirmedRegistrations = confirmedRegistrations;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalParticipants() {
        return totalParticipants;
    }

    public void setTotalParticipants(Long totalParticipants) {
        this.totalParticipants = totalParticipants;
    }

    public Long getCheckedInParticipants() {
        return checkedInParticipants;
    }

    public void setCheckedInParticipants(Long checkedInParticipants) {
        this.checkedInParticipants = checkedInParticipants;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
}

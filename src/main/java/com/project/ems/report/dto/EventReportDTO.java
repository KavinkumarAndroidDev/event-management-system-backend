package com.project.ems.report.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EventReportDTO {

    private Long eventId;
    private String eventTitle;
    private String status;
    private LocalDateTime startTime;
    private Long totalRegistrations;
    private Long confirmedRegistrations;
    private BigDecimal grossRevenue;
    private BigDecimal refundAmount;
    private BigDecimal netRevenue;
    private Long totalParticipants;
    private Long checkedInParticipants;
    private Double attendanceRate;
    private Double averageRating;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
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

    public BigDecimal getGrossRevenue() {
        return grossRevenue;
    }

    public void setGrossRevenue(BigDecimal grossRevenue) {
        this.grossRevenue = grossRevenue;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public BigDecimal getNetRevenue() {
        return netRevenue;
    }

    public void setNetRevenue(BigDecimal netRevenue) {
        this.netRevenue = netRevenue;
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

    public Double getAttendanceRate() {
        return attendanceRate;
    }

    public void setAttendanceRate(Double attendanceRate) {
        this.attendanceRate = attendanceRate;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
}

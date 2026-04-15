package com.project.ems.report.dto;

import java.math.BigDecimal;

public class RevenueDataPoint {

    private String period;
    private BigDecimal revenue;
    private Long registrations;

    public RevenueDataPoint(String period, BigDecimal revenue, Long registrations) {
        this.period = period;
        this.revenue = revenue;
        this.registrations = registrations;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public Long getRegistrations() {
        return registrations;
    }

    public void setRegistrations(Long registrations) {
        this.registrations = registrations;
    }
}

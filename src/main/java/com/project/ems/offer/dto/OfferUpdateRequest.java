package com.project.ems.offer.dto;

import java.time.LocalDateTime;

public class OfferUpdateRequest {

    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Integer totalUsageLimit;

    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }

    public LocalDateTime getValidTo() { return validTo; }
    public void setValidTo(LocalDateTime validTo) { this.validTo = validTo; }

    public Integer getTotalUsageLimit() { return totalUsageLimit; }
    public void setTotalUsageLimit(Integer totalUsageLimit) { this.totalUsageLimit = totalUsageLimit; }
}

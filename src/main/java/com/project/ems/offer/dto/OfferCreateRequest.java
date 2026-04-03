package com.project.ems.offer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class OfferCreateRequest {

    @NotBlank(message = "Offer code is required")
    private String code;

    @NotNull(message = "Discount percentage is required")
    @DecimalMin(value = "0.01")
    @DecimalMax(value = "100.00")
    private BigDecimal discountPercentage;

    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Valid from is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid to is required")
    private LocalDateTime validTo;

    @Positive(message = "Usage limit must be positive")
    private Integer totalUsageLimit;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }

    public BigDecimal getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }

    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }

    public LocalDateTime getValidTo() { return validTo; }
    public void setValidTo(LocalDateTime validTo) { this.validTo = validTo; }

    public Integer getTotalUsageLimit() { return totalUsageLimit; }
    public void setTotalUsageLimit(Integer totalUsageLimit) { this.totalUsageLimit = totalUsageLimit; }
}

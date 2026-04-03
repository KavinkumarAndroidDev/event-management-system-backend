package com.project.ems.offer.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OfferValidateRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotBlank(message = "Offer code is required")
    private String code;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

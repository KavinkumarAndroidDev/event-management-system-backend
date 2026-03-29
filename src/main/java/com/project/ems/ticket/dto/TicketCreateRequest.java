package com.project.ems.ticket.dto;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TicketCreateRequest {

    @NotBlank(message = "Ticket name must not be blank")
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be >= 0")
    private BigDecimal price;

    @NotNull(message = "Ticket quantity is required")
    @Positive(message = "Ticket quantity must be positive")
    private Integer totalQuantity;

    @NotNull(message = "Sale start time is required")
    private LocalDateTime saleStartTime;

    @NotNull(message = "Sale end time is required")
    private LocalDateTime saleEndTime;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Integer getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(Integer totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public LocalDateTime getSaleStartTime() {
		return saleStartTime;
	}

	public void setSaleStartTime(LocalDateTime saleStartTime) {
		this.saleStartTime = saleStartTime;
	}

	public LocalDateTime getSaleEndTime() {
		return saleEndTime;
	}

	public void setSaleEndTime(LocalDateTime saleEndTime) {
		this.saleEndTime = saleEndTime;
	}

    
}
package com.project.ems.booking.dto;

import java.math.BigDecimal;

public class BookingItemResponse {

	private Long id;
	private Long ticketId;
	private String ticketName;
	private Integer quantity;
	private BigDecimal pricePerTicket;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTicketId() {
		return ticketId;
	}

	public void setTicketId(Long ticketId) {
		this.ticketId = ticketId;
	}

	public String getTicketName() {
		return ticketName;
	}

	public void setTicketName(String ticketName) {
		this.ticketName = ticketName;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getPricePerTicket() {
		return pricePerTicket;
	}

	public void setPricePerTicket(BigDecimal pricePerTicket) {
		this.pricePerTicket = pricePerTicket;
	}
}

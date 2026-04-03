package com.project.ems.booking.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BookingPreviewRequest {

	@NotNull(message = "Event ID is required")
	private Long eventId;

	@NotNull
	@Size(min = 1)
	@Valid
	private List<BookingItemRequest> items;

	private String offerCode;

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public List<BookingItemRequest> getItems() {
		return items;
	}

	public void setItems(List<BookingItemRequest> items) {
		this.items = items;
	}

	public String getOfferCode() {
		return offerCode;
	}

	public void setOfferCode(String offerCode) {
		this.offerCode = offerCode;
	}
}

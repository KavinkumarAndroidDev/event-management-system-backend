package com.project.ems.booking.dto;

import java.math.BigDecimal;
import java.util.List;

public class BookingPreviewResponse {

	private Long eventId;
	private List<BookingItemSummary> items;
	private BigDecimal subtotal;
	private BigDecimal discountAmount;
	private BigDecimal totalAmount;
	private String offerCode;

	public static class BookingItemSummary {
		private Long ticketId;
		private String ticketName;
		private Integer qty;
		private BigDecimal pricePerTicket;
		private BigDecimal lineTotal;

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

		public Integer getQty() {
			return qty;
		}

		public void setQty(Integer qty) {
			this.qty = qty;
		}

		public BigDecimal getPricePerTicket() {
			return pricePerTicket;
		}

		public void setPricePerTicket(BigDecimal pricePerTicket) {
			this.pricePerTicket = pricePerTicket;
		}

		public BigDecimal getLineTotal() {
			return lineTotal;
		}

		public void setLineTotal(BigDecimal lineTotal) {
			this.lineTotal = lineTotal;
		}
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public List<BookingItemSummary> getItems() {
		return items;
	}

	public void setItems(List<BookingItemSummary> items) {
		this.items = items;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(BigDecimal discountAmount) {
		this.discountAmount = discountAmount;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getOfferCode() {
		return offerCode;
	}

	public void setOfferCode(String offerCode) {
		this.offerCode = offerCode;
	}
}

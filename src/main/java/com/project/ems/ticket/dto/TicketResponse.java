package com.project.ems.ticket.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TicketResponse {

    private Long id;
    private Long eventId;
    private String name;
    private BigDecimal price;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public LocalDateTime getSaleStartTime() { return saleStartTime; }
    public void setSaleStartTime(LocalDateTime saleStartTime) { this.saleStartTime = saleStartTime; }

    public LocalDateTime getSaleEndTime() { return saleEndTime; }
    public void setSaleEndTime(LocalDateTime saleEndTime) { this.saleEndTime = saleEndTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

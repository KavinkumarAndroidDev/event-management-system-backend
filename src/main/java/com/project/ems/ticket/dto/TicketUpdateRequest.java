package com.project.ems.ticket.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TicketUpdateRequest {

    private BigDecimal price;
    private Integer totalQuantity;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }

    public LocalDateTime getSaleStartTime() { return saleStartTime; }
    public void setSaleStartTime(LocalDateTime saleStartTime) { this.saleStartTime = saleStartTime; }

    public LocalDateTime getSaleEndTime() { return saleEndTime; }
    public void setSaleEndTime(LocalDateTime saleEndTime) { this.saleEndTime = saleEndTime; }
}

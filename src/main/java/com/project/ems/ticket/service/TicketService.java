package com.project.ems.ticket.service;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.common.entity.Event;
import com.project.ems.common.entity.Status;
import com.project.ems.common.entity.Ticket;
import com.project.ems.common.exception.EventNotFoundException;
import com.project.ems.common.exception.TicketNotFoundException;
import com.project.ems.common.exception.UnauthorizedException;
import com.project.ems.event.repository.EventRepository;
import com.project.ems.ticket.dto.TicketCreateRequest;
import com.project.ems.ticket.dto.TicketResponse;
import com.project.ems.ticket.dto.TicketStatusRequest;
import com.project.ems.ticket.dto.TicketUpdateRequest;
import com.project.ems.ticket.repository.TicketRepository;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    public TicketService(TicketRepository ticketRepository, EventRepository eventRepository) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsForEvent(Long eventId, Long userId, String role) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        List<Ticket> tickets = event.getTickets();

        if (role != null && role.equals("ORGANIZER")) {
            if (!event.getOrganizer().getId().equals(userId)) {
                throw new UnauthorizedException("You can only view tickets for your own events");
            }
            return tickets.stream().map(this::toResponse).toList();
        }

        return tickets.stream()
                .filter(t -> t.getStatus() == Status.ACTIVE)
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TicketResponse createTicket(Long eventId, TicketCreateRequest request, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedException("You can only add tickets to your own events");
        }

        validateTicketPrice(request.getPrice());
        validateSaleWindow(request.getSaleStartTime(), request.getSaleEndTime());
        validateVenueCapacity(event, request.getTotalQuantity());

        Ticket ticket = new Ticket();
        ticket.setName(request.getName());
        ticket.setPrice(request.getPrice());
        ticket.setTotalQuantity(request.getTotalQuantity());
        ticket.setAvailableQuantity(request.getTotalQuantity());
        ticket.setSaleStartTime(request.getSaleStartTime());
        ticket.setSaleEndTime(request.getSaleEndTime());
        ticket.setStatus(Status.ACTIVE);
        ticket.setEvent(event);

        return toResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse updateTicket(Long ticketId, TicketUpdateRequest request, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (!ticket.getEvent().getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedException("You can only update tickets for your own events");
        }

        LocalDateTime now = LocalDateTime.now();
        if (ticket.getSaleStartTime() != null && ticket.getSaleStartTime().isBefore(now)) {
            throw new IllegalStateException("Cannot update ticket after sale has started");
        }

        LocalDateTime nextSaleStart = request.getSaleStartTime() != null ? request.getSaleStartTime() : ticket.getSaleStartTime();
        LocalDateTime nextSaleEnd = request.getSaleEndTime() != null ? request.getSaleEndTime() : ticket.getSaleEndTime();
        validateSaleWindow(nextSaleStart, nextSaleEnd);

        if (request.getPrice() != null) {
            validateTicketPrice(request.getPrice());
            ticket.setPrice(request.getPrice());
        }

        if (request.getTotalQuantity() != null) {
            int diff = request.getTotalQuantity() - ticket.getTotalQuantity();
            validateVenueCapacity(ticket.getEvent(), diff);
            ticket.setTotalQuantity(request.getTotalQuantity());
            ticket.setAvailableQuantity(ticket.getAvailableQuantity() + diff);
        }

        if (request.getSaleStartTime() != null) ticket.setSaleStartTime(request.getSaleStartTime());
        if (request.getSaleEndTime() != null) ticket.setSaleEndTime(request.getSaleEndTime());

        return toResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse updateTicketStatus(Long ticketId, TicketStatusRequest request, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (!ticket.getEvent().getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedException("You can only manage tickets for your own events");
        }

        Status newStatus = Status.valueOf(request.getStatus().toUpperCase());
        ticket.setStatus(newStatus);

        return toResponse(ticketRepository.save(ticket));
    }

    private TicketResponse toResponse(Ticket ticket) {
        TicketResponse res = new TicketResponse();
        res.setId(ticket.getId());
        res.setName(ticket.getName());
        res.setPrice(ticket.getPrice());
        res.setTotalQuantity(ticket.getTotalQuantity());
        res.setAvailableQuantity(ticket.getAvailableQuantity());
        res.setSaleStartTime(ticket.getSaleStartTime());
        res.setSaleEndTime(ticket.getSaleEndTime());
        res.setStatus(ticket.getStatus() != null ? ticket.getStatus().name() : null);

        if (ticket.getEvent() != null) {
            res.setEventId(ticket.getEvent().getId());
        }

        return res;
    }

    private void validateTicketPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Ticket price must be at least 1");
        }
    }

    private void validateSaleWindow(LocalDateTime saleStartTime, LocalDateTime saleEndTime) {
        if (saleStartTime == null || saleEndTime == null) {
            throw new IllegalArgumentException("Ticket sale start and end time are required");
        }
        if (!saleEndTime.isAfter(saleStartTime)) {
            throw new IllegalArgumentException("Ticket sale end time must be after sale start time");
        }
    }

    private void validateVenueCapacity(Event event, int addedQuantity) {
        if (event.getVenue() == null || event.getVenue().getCapacity() == null) return;

        int currentQuantity = event.getTickets() == null ? 0 : event.getTickets()
                .stream()
                .mapToInt(Ticket::getTotalQuantity)
                .sum();

        if (currentQuantity + addedQuantity > event.getVenue().getCapacity()) {
            throw new IllegalArgumentException("Total ticket quantity cannot be greater than venue capacity");
        }
    }
}

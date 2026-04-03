package com.project.ems.ticket.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.ticket.dto.TicketCreateRequest;
import com.project.ems.ticket.dto.TicketResponse;
import com.project.ems.ticket.dto.TicketStatusRequest;
import com.project.ems.ticket.dto.TicketUpdateRequest;
import com.project.ems.ticket.service.TicketService;

import jakarta.validation.Valid;

@RestController
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/events/{eventId}/tickets")
    public ResponseEntity<List<TicketResponse>> getEventTickets(
            @PathVariable Long eventId,
            Authentication auth,
            @AuthenticationPrincipal Long userId
    ) {
        String role = (auth != null && auth.isAuthenticated())
                ? auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
                : null;
        return ResponseEntity.ok(ticketService.getTicketsForEvent(eventId, userId, role));
    }

    @PostMapping("/events/{eventId}/tickets")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<TicketResponse> createTicket(
            @PathVariable Long eventId,
            @Valid @RequestBody TicketCreateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(eventId, request, userId));
    }

    @PutMapping("/tickets/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable Long id,
            @RequestBody TicketUpdateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request, userId));
    }

    @PatchMapping("/tickets/{id}/status")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<TicketResponse> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody TicketStatusRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(ticketService.updateTicketStatus(id, request, userId));
    }
}

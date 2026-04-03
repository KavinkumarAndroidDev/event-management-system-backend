package com.project.ems.booking.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.booking.dto.BookingDetailResponse;
import com.project.ems.booking.service.EventRegistrationService;

@RestController
@PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
public class EventRegistrationController {

    private final EventRegistrationService eventRegistrationService;

    public EventRegistrationController(EventRegistrationService eventRegistrationService) {
        this.eventRegistrationService = eventRegistrationService;
    }

    @GetMapping("/events/{eventId}/registrations")
    public ResponseEntity<Page<BookingDetailResponse>> getEventRegistrations(
            @PathVariable Long eventId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Long userId,
            Authentication auth
    ) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return ResponseEntity.ok(eventRegistrationService.getRegistrationsForEvent(eventId, status, page, size, userId, role));
    }

    @GetMapping("/events/{eventId}/registrations/{id}")
    public ResponseEntity<BookingDetailResponse> getEventRegistration(
            @PathVariable Long eventId,
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            Authentication auth
    ) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return ResponseEntity.ok(eventRegistrationService.getRegistrationById(eventId, id, userId, role));
    }
}

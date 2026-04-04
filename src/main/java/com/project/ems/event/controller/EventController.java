package com.project.ems.event.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.event.dto.EventCreateRequest;
import com.project.ems.event.dto.EventDetailDTO;
import com.project.ems.event.dto.EventListDTO;
import com.project.ems.event.dto.EventStatusRequest;
import com.project.ems.event.dto.EventUpdateRequest;
import com.project.ems.event.service.EventService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/events")
@CrossOrigin("http://127.0.0.1:5500/")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    private String resolveRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities() == null) {
            return "PUBLIC";
        }
        return auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("PUBLIC");
    }

    @GetMapping
    public ResponseEntity<Page<EventListDTO>> getEvents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime,asc") String sort,
            @AuthenticationPrincipal Long userId
    ) {
        String role = resolveRole();
        return ResponseEntity.ok(
                eventService.getEvents(
                        search, categoryId, city, venueId,
                        date, minPrice, maxPrice, status,
                        userId, role, page, size, sort
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDetailDTO> getEventById(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        String role = resolveRole();
        return ResponseEntity.ok(eventService.getEventById(id, userId, role));
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventDetailDTO> createEvent(
            @Valid @RequestBody EventCreateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request, userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventDetailDTO> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(eventService.updateEvent(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        eventService.deleteEvent(id, userId);
        return ResponseEntity.ok("Event deleted");
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<EventDetailDTO> changeEventStatus(
            @PathVariable Long id,
            @Valid @RequestBody EventStatusRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(eventService.changeStatus(id, request, userId));
    }
}

package com.project.ems.venue.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.common.entity.Venue;
import com.project.ems.venue.dto.VenueCreateRequest;
import com.project.ems.venue.dto.VenueStatusRequest;
import com.project.ems.venue.dto.VenueUpdateRequest;
import com.project.ems.venue.service.VenueService;

import jakarta.validation.Valid;

@RestController
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    // GET /venues — PUBLIC (optional ?city= filter)
    @GetMapping("/venues")
    public ResponseEntity<List<Venue>> listActiveVenues(@RequestParam(required = false) String city) {
        List<Venue> venues = venueService.listActiveVenues(city);
        return ResponseEntity.ok(venues);
    }

    // GET /venues/{id} — PUBLIC
    @GetMapping("/venues/{id}")
    public ResponseEntity<Venue> getVenueById(@PathVariable Long id) {
        Venue venue = venueService.getVenueById(id);
        return ResponseEntity.ok(venue);
    }

    // POST /venues — ADMIN only
    @PostMapping("/venues")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Venue> createVenue(@Valid @RequestBody VenueCreateRequest request) {
        Venue venue = venueService.createVenue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(venue);
    }

    // PUT /venues/{id} — ADMIN only
    @PutMapping("/venues/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Venue> updateVenue(@PathVariable Long id,
                                             @Valid @RequestBody VenueUpdateRequest request) {
        Venue venue = venueService.updateVenue(id, request);
        return ResponseEntity.ok(venue);
    }

    // PATCH /venues/{id}/status — ADMIN only
    @PatchMapping("/venues/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Venue> updateVenueStatus(@PathVariable Long id,
                                                   @Valid @RequestBody VenueStatusRequest request) {
        Venue venue = venueService.updateVenueStatus(id, request);
        return ResponseEntity.ok(venue);
    }
}

package com.project.ems.offer.controller;

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

import com.project.ems.offer.dto.OfferCreateRequest;
import com.project.ems.offer.dto.OfferResponse;
import com.project.ems.offer.dto.OfferStatusRequest;
import com.project.ems.offer.dto.OfferUpdateRequest;
import com.project.ems.offer.dto.OfferValidateRequest;
import com.project.ems.offer.dto.OfferValidateResponse;
import com.project.ems.offer.service.OfferService;

import jakarta.validation.Valid;

@RestController
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping("/events/{eventId}/offers")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<List<OfferResponse>> getOffersForEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal Long userId,
            Authentication auth
    ) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return ResponseEntity.ok(offerService.getOffersForEvent(eventId, userId, role));
    }

    @PostMapping("/events/{eventId}/offers")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<OfferResponse> createOffer(
            @PathVariable Long eventId,
            @Valid @RequestBody OfferCreateRequest request,
            @AuthenticationPrincipal Long userId,
            Authentication auth
    ) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return ResponseEntity.status(HttpStatus.CREATED).body(offerService.createOffer(eventId, request, userId, role));
    }

    @PutMapping("/offers/{id}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<OfferResponse> updateOffer(
            @PathVariable Long id,
            @RequestBody OfferUpdateRequest request,
            @AuthenticationPrincipal Long userId,
            Authentication auth
    ) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return ResponseEntity.ok(offerService.updateOffer(id, request, userId, role));
    }

    @PatchMapping("/offers/{id}/status")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<OfferResponse> updateOfferStatus(
            @PathVariable Long id,
            @Valid @RequestBody OfferStatusRequest request,
            @AuthenticationPrincipal Long userId,
            Authentication auth
    ) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return ResponseEntity.ok(offerService.updateOfferStatus(id, request, userId, role));
    }

    @PostMapping("/offers/validate")
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<OfferValidateResponse> validateOffer(@Valid @RequestBody OfferValidateRequest request) {
        return ResponseEntity.ok(offerService.validateOffer(request));
    }
}

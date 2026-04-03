package com.project.ems.participant.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.participant.dto.ParticipantResponse;
import com.project.ems.participant.dto.ParticipantStatusRequest;
import com.project.ems.participant.dto.ParticipantUpdateRequest;
import com.project.ems.participant.service.ParticipantService;

import jakarta.validation.Valid;

@RestController
public class ParticipantController {

    private final ParticipantService participantService;

    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @GetMapping("/events/{eventId}/participants")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Page<ParticipantResponse>> getParticipants(
            @PathVariable Long eventId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(participantService.getParticipantsForEvent(eventId, status, page, size, userId));
    }

    @GetMapping("/events/{eventId}/participants/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ParticipantResponse> getParticipant(
            @PathVariable Long eventId,
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(participantService.getParticipantById(eventId, id, userId));
    }

    @PatchMapping("/participants/{id}/status")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ATTENDEE')")
    public ResponseEntity<ParticipantResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ParticipantStatusRequest request,
            @AuthenticationPrincipal Long userId,
            Authentication auth
    ) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return ResponseEntity.ok(participantService.updateStatus(id, request, userId, role));
    }

    @PutMapping("/participants/{id}")
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<ParticipantResponse> updateParticipant(
            @PathVariable Long id,
            @RequestBody ParticipantUpdateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(participantService.updateParticipant(id, request, userId));
    }
}

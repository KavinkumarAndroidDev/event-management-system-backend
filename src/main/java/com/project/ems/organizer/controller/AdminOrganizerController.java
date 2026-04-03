package com.project.ems.organizer.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.organizer.dto.OrganizerProfileDTO;
import com.project.ems.organizer.dto.OrganizerStatusRequest;
import com.project.ems.organizer.service.OrganizerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/organizer-profiles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrganizerController {

    private final OrganizerService organizerService;

    public AdminOrganizerController(OrganizerService organizerService) {
        this.organizerService = organizerService;
    }

    @GetMapping
    public ResponseEntity<Page<OrganizerProfileDTO>> getAllOrganizers(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Page<OrganizerProfileDTO> result = organizerService.getAllOrganizers(status, page, size, sort);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrganizerProfileDTO> updateOrganizerStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrganizerStatusRequest request
    ) {
        OrganizerProfileDTO profile = organizerService.updateOrganizerStatus(id, request);
        return ResponseEntity.ok(profile);
    }
}

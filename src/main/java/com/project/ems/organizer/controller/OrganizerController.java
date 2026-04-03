package com.project.ems.organizer.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.common.exception.UnauthorizedException;
import com.project.ems.organizer.dto.OrganizerOnboardingRequest;
import com.project.ems.organizer.dto.OrganizerProfileDTO;
import com.project.ems.organizer.dto.OrganizerUpdateRequest;
import com.project.ems.organizer.service.OrganizerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class OrganizerController {

    private final OrganizerService organizerService;

    public OrganizerController(OrganizerService organizerService) {
        this.organizerService = organizerService;
    }

    @PostMapping("/{id}/organizer-profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrganizerProfileDTO> registerOrganizerProfile(
            @PathVariable Long id,
            @Valid @RequestBody OrganizerOnboardingRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        if (!id.equals(userId)) {
            throw new UnauthorizedException("You can only register an organizer profile for your own account");
        }
        OrganizerProfileDTO profile = organizerService.onboard(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }

    @GetMapping("/{id}/organizer-profile")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<OrganizerProfileDTO> getOrganizerProfile(@PathVariable Long id) {
        OrganizerProfileDTO profile = organizerService.getProfileByUserId(id);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/{id}/organizer-profile")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<OrganizerProfileDTO> updateOrganizerProfile(
            @PathVariable Long id,
            @RequestBody OrganizerUpdateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        if (!id.equals(userId)) {
            throw new UnauthorizedException("You can only update your own organizer profile");
        }
        OrganizerProfileDTO profile = organizerService.updateMyProfile(id, request);
        return ResponseEntity.ok(profile);
    }
}

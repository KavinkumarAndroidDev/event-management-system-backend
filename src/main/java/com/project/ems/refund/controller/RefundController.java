package com.project.ems.refund.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.refund.dto.RefundResponse;
import com.project.ems.refund.service.RefundService;

@RestController
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @GetMapping("/events/{eventId}/refunds")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Page<RefundResponse>> getRefundsForEvent(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(refundService.getRefundsForEvent(eventId, userId, page, size));
    }

    @GetMapping("/refunds")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<RefundResponse>> getAllRefunds(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(refundService.getAllRefunds(status, page, size));
    }

    @PostMapping("/refunds/{id}/retry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RefundResponse> retryRefund(@PathVariable Long id) {
        return ResponseEntity.ok(refundService.retryRefund(id));
    }
}

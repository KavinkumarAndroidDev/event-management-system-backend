package com.project.ems.booking.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.booking.dto.BookingCreateRequest;
import com.project.ems.booking.dto.BookingCreateResponse;
import com.project.ems.booking.dto.BookingDetailResponse;
import com.project.ems.booking.dto.BookingPreviewRequest;
import com.project.ems.booking.dto.BookingPreviewResponse;
import com.project.ems.booking.dto.BookingStatusRequest;
import com.project.ems.booking.service.BookingService;
import com.project.ems.refund.dto.RefundResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/bookings")
@PreAuthorize("hasRole('ATTENDEE')")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/preview")
    public ResponseEntity<BookingPreviewResponse> previewBooking(
            @Valid @RequestBody BookingPreviewRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(bookingService.preview(request, userId));
    }

    @PostMapping
    public ResponseEntity<BookingCreateResponse> createBooking(
            @Valid @RequestBody BookingCreateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request, userId));
    }

    @GetMapping
    public ResponseEntity<Page<BookingDetailResponse>> getMyBookings(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(bookingService.getMyBookings(userId, status, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDetailResponse> getBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(bookingService.getBookingById(id, userId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BookingDetailResponse> cancelBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingStatusRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, request, userId));
    }

    @GetMapping("/{bookingId}/refunds")
    public ResponseEntity<List<RefundResponse>> getRefundsForBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(bookingService.getRefundsForBooking(bookingId, userId));
    }
}

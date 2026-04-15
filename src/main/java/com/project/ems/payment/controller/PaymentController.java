package com.project.ems.payment.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.payment.dto.PaymentFailRequest;
import com.project.ems.payment.dto.PaymentResponse;
import com.project.ems.payment.dto.PaymentRetryRequest;
import com.project.ems.payment.dto.PaymentVerifyRequest;
import com.project.ems.payment.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/payments")
@CrossOrigin("http://127.0.0.1:5500/")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(paymentService.verifyPayment(request, userId));
    }

    @PostMapping("/fail")
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<String> markFailed(
            @Valid @RequestBody PaymentFailRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        paymentService.markFailed(request, userId);
        return ResponseEntity.ok("Payment marked as failed");
    }

    @PostMapping("/retry")
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<Map<String, String>> retryPayment(
            @Valid @RequestBody PaymentRetryRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        String orderId = paymentService.retryPayment(request, userId);
        return ResponseEntity.ok(Map.of("razorpayOrderId", orderId));
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(paymentService.getPaymentForBooking(bookingId, userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(paymentService.getAllPayments(status, page, size));
    }

    @GetMapping("/my-payments")
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<Page<PaymentResponse>> getUserPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(paymentService.getUserPayments(userId, page, size));
    }
}

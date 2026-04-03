package com.project.ems.feedback.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.feedback.dto.FeedbackCreateRequest;
import com.project.ems.feedback.dto.FeedbackResponse;
import com.project.ems.feedback.dto.FeedbackStatusRequest;
import com.project.ems.feedback.dto.FeedbackUpdateRequest;
import com.project.ems.feedback.service.FeedbackService;

import jakarta.validation.Valid;

@RestController
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/events/{eventId}/feedbacks")
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<FeedbackResponse> submitFeedback(
            @PathVariable Long eventId,
            @Valid @RequestBody FeedbackCreateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        FeedbackResponse feedback = feedbackService.submitFeedback(eventId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(feedback);
    }

    @GetMapping("/events/{eventId}/feedbacks")
    public ResponseEntity<Page<FeedbackResponse>> getFeedbacks(
            @PathVariable Long eventId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth
    ) {
        String role = (auth != null && auth.isAuthenticated())
                ? auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
                : null;
        Page<FeedbackResponse> feedbacks = feedbackService.getFeedbacksForEvent(eventId, rating, status, role, page, size);
        return ResponseEntity.ok(feedbacks);
    }

    @PutMapping("/feedbacks/{id}")
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<FeedbackResponse> updateFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackUpdateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(feedbackService.updateFeedback(id, request, userId));
    }

    @DeleteMapping("/feedbacks/{id}")
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<String> deleteFeedback(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        feedbackService.deleteFeedback(id, userId);
        return ResponseEntity.ok("Feedback deleted");
    }

    @PatchMapping("/feedbacks/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeedbackResponse> moderateFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackStatusRequest request
    ) {
        return ResponseEntity.ok(feedbackService.moderateFeedback(id, request));
    }
}

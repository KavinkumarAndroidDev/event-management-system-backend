package com.project.ems.notification.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.notification.dto.BroadcastLogResponse;
import com.project.ems.notification.dto.BroadcastRequest;
import com.project.ems.notification.dto.NotificationReadRequest;
import com.project.ems.notification.dto.NotificationResponse;
import com.project.ems.notification.dto.SendAnnouncementRequest;
import com.project.ems.notification.service.NotificationService;

import jakarta.validation.Valid;

@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(notificationService.getMyNotifications(userId, isRead, page, size));
    }

    @GetMapping("/notifications/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PatchMapping("/notifications/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @Valid @RequestBody NotificationReadRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(notificationService.markAsRead(id, userId, request));
    }

    @PostMapping("/notifications/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> markAllRead(@AuthenticationPrincipal Long userId) {
        notificationService.markAllRead(userId);
        return ResponseEntity.ok("All notifications marked as read");
    }

    @PostMapping("/events/{eventId}/notifications")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> sendEventAnnouncement(
            @PathVariable Long eventId,
            @Valid @RequestBody SendAnnouncementRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        notificationService.sendEventAnnouncement(eventId, request, userId);
        return ResponseEntity.ok("Announcement sent");
    }

    @PostMapping("/notifications/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> broadcast(
            @Valid @RequestBody BroadcastRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        notificationService.broadcast(request, userId);
        return ResponseEntity.ok("Broadcast sent");
    }

    @GetMapping("/notifications/broadcast-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<BroadcastLogResponse>> getBroadcastHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(notificationService.getBroadcastHistory(page, size));
    }
}

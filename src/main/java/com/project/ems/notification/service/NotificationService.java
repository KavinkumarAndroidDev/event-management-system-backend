package com.project.ems.notification.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.auth.repository.UserRepository;
import com.project.ems.common.entity.BroadcastLog;
import com.project.ems.common.entity.Notification;
import com.project.ems.common.entity.User;
import com.project.ems.common.exception.EventNotFoundException;
import com.project.ems.common.exception.UnauthorizedException;
import com.project.ems.common.exception.UserNotFoundException;
import com.project.ems.event.repository.EventRepository;
import com.project.ems.notification.dto.BroadcastLogResponse;
import com.project.ems.notification.dto.BroadcastRequest;
import com.project.ems.notification.dto.NotificationReadRequest;
import com.project.ems.notification.dto.NotificationResponse;
import com.project.ems.notification.dto.SendAnnouncementRequest;
import com.project.ems.notification.repository.BroadcastLogRepository;
import com.project.ems.notification.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final BroadcastLogRepository broadcastLogRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               EventRepository eventRepository,
                               BroadcastLogRepository broadcastLogRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.broadcastLogRepository = broadcastLogRepository;
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Long userId, Boolean isRead, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (isRead != null) {
            return notificationRepository.findByUserIdAndIsRead(userId, isRead, pageable).map(this::toResponse);
        }

        return notificationRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getUnreadCount(Long userId) {
        long count = notificationRepository.countByUserIdAndIsRead(userId, false);
        return Map.of("unreadCount", count);
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId, NotificationReadRequest request) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own notifications");
        }

        notification.setIsRead(request.getIsRead());
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadForUser(userId);
    }

    @Transactional
    public void sendEventAnnouncement(Long eventId, SendAnnouncementRequest request, Long organizerId) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new UnauthorizedException("You can only send announcements for your own events");
        }

        List<User> confirmedAttendees = userRepository.findConfirmedAttendeesForEvent(eventId);

        for (User user : confirmedAttendees) {
            Notification n = new Notification();
            n.setUser(user);
            n.setTitle(request.getTitle());
            n.setMessage(request.getMessage());
            n.setType("EVENT_ANNOUNCEMENT");
            n.setIsRead(false);
            n.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(n);
        }
    }

    @Transactional
    public void broadcast(BroadcastRequest request, Long adminId) {
        List<User> users;

        if (request.getTargetRole() != null && !request.getTargetRole().isBlank()) {
            users = userRepository.findAll().stream()
                    .filter(u -> u.getRole().getName().name().equalsIgnoreCase(request.getTargetRole()))
                    .toList();
        } else {
            users = userRepository.findAll();
        }

        for (User user : users) {
            Notification n = new Notification();
            n.setUser(user);
            n.setTitle(request.getTitle());
            n.setMessage(request.getMessage());
            n.setType("BROADCAST");
            n.setIsRead(false);
            n.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(n);
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Admin not found"));

        BroadcastLog log = new BroadcastLog();
        log.setTitle(request.getTitle());
        log.setMessage(request.getMessage());
        log.setTargetRole(request.getTargetRole());
        log.setRecipientCount(users.size());
        log.setCreatedAt(LocalDateTime.now());
        log.setSentBy(admin);
        broadcastLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<BroadcastLogResponse> getBroadcastHistory(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return broadcastLogRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toLogResponse);
    }

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse res = new NotificationResponse();
        res.setId(n.getId());
        res.setTitle(n.getTitle());
        res.setMessage(n.getMessage());
        res.setType(n.getType());
        res.setIsRead(n.getIsRead());
        res.setCreatedAt(n.getCreatedAt());
        return res;
    }

    private BroadcastLogResponse toLogResponse(BroadcastLog log) {
        BroadcastLogResponse res = new BroadcastLogResponse();
        res.setId(log.getId());
        res.setTitle(log.getTitle());
        res.setMessage(log.getMessage());
        res.setTargetRole(log.getTargetRole());
        res.setRecipientCount(log.getRecipientCount());
        res.setCreatedAt(log.getCreatedAt());
        if (log.getSentBy() != null) {
            res.setSentByName(log.getSentBy().getFullName());
        }
        return res;
    }
}

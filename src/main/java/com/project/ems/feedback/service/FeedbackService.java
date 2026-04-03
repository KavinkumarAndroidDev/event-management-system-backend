package com.project.ems.feedback.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.auth.repository.UserRepository;
import com.project.ems.common.entity.Event;
import com.project.ems.common.entity.Feedback;
import com.project.ems.common.entity.Feedback.FeedbackStatus;
import com.project.ems.common.entity.User;
import com.project.ems.common.exception.DuplicateFeedbackException;
import com.project.ems.common.exception.EventNotFoundException;
import com.project.ems.common.exception.FeedbackNotFoundException;
import com.project.ems.common.exception.UnauthorizedException;
import com.project.ems.common.exception.UserNotFoundException;
import com.project.ems.event.repository.EventRepository;
import com.project.ems.feedback.dto.FeedbackCreateRequest;
import com.project.ems.feedback.dto.FeedbackResponse;
import com.project.ems.feedback.dto.FeedbackStatusRequest;
import com.project.ems.feedback.dto.FeedbackUpdateRequest;
import com.project.ems.feedback.repository.FeedbackRepository;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           EventRepository eventRepository,
                           UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FeedbackResponse submitFeedback(Long eventId, FeedbackCreateRequest request, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (event.getStatus() != Event.EventStatus.COMPLETED) {
            throw new IllegalStateException("Feedback can only be submitted for completed events");
        }

        if (feedbackRepository.findByUserIdAndEventId(userId, eventId).isPresent()) {
            throw new DuplicateFeedbackException("You have already submitted feedback for this event");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setEvent(event);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        feedback.setStatus(FeedbackStatus.VISIBLE);
        feedback.setCreatedAt(LocalDateTime.now());

        return toResponse(feedbackRepository.save(feedback));
    }

    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getFeedbacksForEvent(Long eventId, Integer rating, String status, String role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        if (role != null && (role.equals("ORGANIZER") || role.equals("ADMIN"))) {
            if (status != null) {
                FeedbackStatus fs = FeedbackStatus.valueOf(status.toUpperCase());
                return feedbackRepository.findByEventIdAndStatus(eventId, fs, pageable).map(this::toResponse);
            }
            return feedbackRepository.findByEventId(eventId, pageable).map(this::toResponse);
        }

        return feedbackRepository.findByEventIdAndStatus(eventId, FeedbackStatus.VISIBLE, pageable).map(this::toResponse);
    }

    @Transactional
    public FeedbackResponse updateFeedback(Long feedbackId, FeedbackUpdateRequest request, Long userId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new FeedbackNotFoundException(feedbackId));

        if (!feedback.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only edit your own feedback");
        }

        long daysSinceSubmission = ChronoUnit.DAYS.between(feedback.getCreatedAt(), LocalDateTime.now());
        if (daysSinceSubmission > 7) {
            throw new IllegalStateException("Feedback can only be edited within 7 days of submission");
        }

        if (request.getRating() != null) feedback.setRating(request.getRating());
        if (request.getComment() != null) feedback.setComment(request.getComment());

        return toResponse(feedbackRepository.save(feedback));
    }

    @Transactional
    public void deleteFeedback(Long feedbackId, Long userId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new FeedbackNotFoundException(feedbackId));

        if (!feedback.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own feedback");
        }

        feedbackRepository.delete(feedback);
    }

    @Transactional
    public FeedbackResponse moderateFeedback(Long feedbackId, FeedbackStatusRequest request) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new FeedbackNotFoundException(feedbackId));

        FeedbackStatus newStatus = FeedbackStatus.valueOf(request.getStatus().toUpperCase());
        feedback.setStatus(newStatus);

        return toResponse(feedbackRepository.save(feedback));
    }

    private FeedbackResponse toResponse(Feedback feedback) {
        FeedbackResponse res = new FeedbackResponse();
        res.setId(feedback.getId());
        res.setRating(feedback.getRating());
        res.setComment(feedback.getComment());
        res.setStatus(feedback.getStatus() != null ? feedback.getStatus().name() : null);
        res.setCreatedAt(feedback.getCreatedAt());

        if (feedback.getUser() != null) {
            res.setUserId(feedback.getUser().getId());
            res.setUserName(feedback.getUser().getFullName());
        }

        if (feedback.getEvent() != null) {
            res.setEventId(feedback.getEvent().getId());
        }

        return res;
    }
}

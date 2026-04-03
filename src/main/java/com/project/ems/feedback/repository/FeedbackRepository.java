package com.project.ems.feedback.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.ems.common.entity.Feedback;
import com.project.ems.common.entity.Feedback.FeedbackStatus;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findByUserIdAndEventId(Long userId, Long eventId);

    Page<Feedback> findByEventId(Long eventId, Pageable pageable);

    Page<Feedback> findByEventIdAndStatus(Long eventId, FeedbackStatus status, Pageable pageable);

    List<Feedback> findByEventId(Long eventId);
}

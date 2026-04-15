package com.project.ems.feedback.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.ems.common.entity.Feedback;
import com.project.ems.common.entity.Feedback.FeedbackStatus;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findByUserIdAndEventId(Long userId, Long eventId);

    Page<Feedback> findByEventId(Long eventId, Pageable pageable);

    Page<Feedback> findByEventIdAndStatus(Long eventId, FeedbackStatus status, Pageable pageable);

    List<Feedback> findByEventId(Long eventId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.event.id = :eventId AND f.status = 'VISIBLE'")
    Double findAverageRatingByEventId(@Param("eventId") Long eventId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.event.organizer.id = :organizerId AND f.status = 'VISIBLE'")
    Double findAverageRatingByOrganizerId(@Param("organizerId") Long organizerId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.status = 'VISIBLE'")
    Double findOverallAverageRating();
}

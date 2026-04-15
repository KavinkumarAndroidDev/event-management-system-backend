package com.project.ems.refund.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.ems.common.entity.Refund;
import com.project.ems.common.entity.Refund.RefundStatus;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByParticipantId(Long participantId);

    @Query("SELECT r FROM Refund r WHERE r.participant.registrationItem.registration.id = :registrationId")
    List<Refund> findByRegistrationId(Long registrationId);

    @Query("SELECT r FROM Refund r WHERE r.participant.event.id = :eventId")
    Page<Refund> findByEventId(Long eventId, Pageable pageable);

    Page<Refund> findByStatus(RefundStatus status, Pageable pageable);

    Optional<Refund> findByParticipantIdAndStatus(Long participantId, RefundStatus status);

    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM Refund r WHERE r.participant.event.id = :eventId AND r.status = 'SUCCESS'")
    java.math.BigDecimal sumSuccessfulRefundsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM Refund r WHERE r.participant.event.organizer.id = :organizerId AND r.status = 'SUCCESS'")
    java.math.BigDecimal sumSuccessfulRefundsByOrganizerId(@Param("organizerId") Long organizerId);

    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM Refund r WHERE r.status = 'SUCCESS'")
    java.math.BigDecimal sumAllSuccessfulRefunds();
}

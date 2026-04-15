package com.project.ems.booking.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.ems.common.entity.Registration;
import com.project.ems.common.entity.Registration.RegistrationStatus;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    Page<Registration> findByUserId(Long userId, Pageable pageable);

    Page<Registration> findByUserIdAndStatus(Long userId, RegistrationStatus status, Pageable pageable);

    Page<Registration> findByEventId(Long eventId, Pageable pageable);

    Page<Registration> findByEventIdAndStatus(Long eventId, RegistrationStatus status, Pageable pageable);

    List<Registration> findByEventId(Long eventId);

    List<Registration> findByStatusAndCreatedAtBefore(RegistrationStatus status, LocalDateTime dateTime);

    @Query("SELECT COUNT(ri) > 0 FROM RegistrationItem ri JOIN ri.registration r WHERE r.user.id = :userId AND ri.id = :registrationItemId")
    boolean existsByUserIdAndRegistrationItemId(@Param("userId") Long userId, @Param("registrationItemId") Long registrationItemId);

    @Query("SELECT COUNT(r) > 0 FROM Registration r WHERE r.user.id = :userId AND r.event.id = :eventId AND r.status IN ('PENDING', 'CONFIRMED')")
    boolean existsActiveBookingByUserIdAndEventId(@Param("userId") Long userId, @Param("eventId") Long eventId);

    @Query("SELECT r FROM Registration r WHERE r.user.id = :userId AND r.event.id = :eventId AND r.status = 'PENDING'")
    Optional<Registration> findPendingBookingByUserIdAndEventId(@Param("userId") Long userId, @Param("eventId") Long eventId);

    @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.organizer.id = :organizerId AND r.status = 'CONFIRMED'")
    Long countConfirmedByOrganizerId(@Param("organizerId") Long organizerId);

    @Query("SELECT COUNT(r) FROM Registration r WHERE r.status = 'CONFIRMED'")
    Long countAllConfirmed();

    @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.organizer.id = :organizerId")
    Long countAllByOrganizerId(@Param("organizerId") Long organizerId);

    @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    Long countConfirmedByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId")
    Long countAllByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COALESCE(SUM(r.totalAmount), 0) FROM Registration r WHERE r.event.organizer.id = :organizerId AND r.status = 'CONFIRMED'")
    BigDecimal sumRevenueByOrganizerId(@Param("organizerId") Long organizerId);

    @Query("SELECT COALESCE(SUM(r.totalAmount), 0) FROM Registration r WHERE r.status = 'CONFIRMED'")
    BigDecimal sumAllRevenue();

    @Query("SELECT COALESCE(SUM(r.totalAmount), 0) FROM Registration r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    BigDecimal sumRevenueByEventId(@Param("eventId") Long eventId);

    @Query("SELECT r FROM Registration r WHERE r.status = 'CONFIRMED' AND r.createdAt >= :from AND r.createdAt <= :to AND (:organizerId IS NULL OR r.event.organizer.id = :organizerId)")
    List<Registration> findConfirmedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("organizerId") Long organizerId);
}

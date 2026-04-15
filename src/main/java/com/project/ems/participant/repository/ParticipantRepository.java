package com.project.ems.participant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.ems.common.entity.Participant;
import com.project.ems.common.entity.Participant.ParticipantStatus;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    @Query("SELECT p FROM Participant p WHERE p.registrationItem.id = :registrationItemId")
    List<Participant> findByRegistrationItemId(Long registrationItemId);

    Page<Participant> findByEventId(Long eventId, Pageable pageable);

    Page<Participant> findByEventIdAndStatus(Long eventId, ParticipantStatus status, Pageable pageable);

    List<Participant> findByEventId(Long eventId);

    boolean existsByRegistrationItemId(Long registrationItemId);

    List<Participant> findByRegistrationItemIdIn(List<Long> registrationItemIds);

    @Query("SELECT COUNT(p) FROM Participant p WHERE p.event.id = :eventId AND p.status = 'CHECKED_IN'")
    Long countCheckedInByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(p) FROM Participant p WHERE p.event.id = :eventId AND p.status != 'CANCELLED'")
    Long countActiveByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(p) FROM Participant p WHERE p.event.organizer.id = :organizerId AND p.status != 'CANCELLED'")
    Long countActiveByOrganizerId(@Param("organizerId") Long organizerId);

    @Query("SELECT COUNT(p) FROM Participant p WHERE p.status != 'CANCELLED'")
    Long countAllActive();

    @Query("SELECT COUNT(p) FROM Participant p WHERE p.event.organizer.id = :organizerId AND p.status = 'CHECKED_IN'")
    Long countCheckedInByOrganizerId(@Param("organizerId") Long organizerId);

    @Query("SELECT COUNT(p) FROM Participant p WHERE p.status = 'CHECKED_IN'")
    Long countAllCheckedIn();

	long countByRegistrationItemId(Long id);

    Optional<Participant> findByEventIdAndEmail(Long eventId, String email);

    Optional<Participant> findByEventIdAndPhone(Long eventId, String phone);

    void deleteByRegistrationItemRegistrationId(Long registrationId);
}

package com.project.ems.participant.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.ems.common.entity.Participant;
import com.project.ems.common.entity.Participant.ParticipantStatus;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    List<Participant> findByRegistrationItemId(Long registrationItemId);

    Page<Participant> findByEventId(Long eventId, Pageable pageable);

    Page<Participant> findByEventIdAndStatus(Long eventId, ParticipantStatus status, Pageable pageable);

    List<Participant> findByEventId(Long eventId);
}

package com.project.ems.participant.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.booking.repository.RegistrationRepository;
import com.project.ems.common.entity.Event;
import com.project.ems.common.entity.Participant;
import com.project.ems.common.entity.Participant.ParticipantStatus;
import com.project.ems.common.entity.Refund;
import com.project.ems.common.exception.EventNotFoundException;
import com.project.ems.common.exception.ParticipantNotFoundException;
import com.project.ems.common.exception.UnauthorizedException;
import com.project.ems.event.repository.EventRepository;
import com.project.ems.participant.dto.ParticipantResponse;
import com.project.ems.participant.dto.ParticipantStatusRequest;
import com.project.ems.participant.dto.ParticipantUpdateRequest;
import com.project.ems.participant.repository.ParticipantRepository;
import com.project.ems.refund.repository.RefundRepository;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final RefundRepository refundRepository;

    public ParticipantService(ParticipantRepository participantRepository,
                              EventRepository eventRepository,
                              RegistrationRepository registrationRepository,
                              RefundRepository refundRepository) {
        this.participantRepository = participantRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.refundRepository = refundRepository;
    }

    @Transactional(readOnly = true)
    public Page<ParticipantResponse> getParticipantsForEvent(Long eventId, String status, int page, int size, Long userId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedException("You can only view participants for your own events");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));

        if (status != null && !status.isBlank()) {
            ParticipantStatus ps = ParticipantStatus.valueOf(status.toUpperCase());
            return participantRepository.findByEventIdAndStatus(eventId, ps, pageable).map(this::toResponse);
        }

        return participantRepository.findByEventId(eventId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ParticipantResponse getParticipantById(Long eventId, Long participantId, Long userId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedException("You can only view participants for your own events");
        }

        Participant p = participantRepository.findById(participantId)
                .orElseThrow(() -> new ParticipantNotFoundException(participantId));

        if (!p.getEvent().getId().equals(eventId)) {
            throw new ParticipantNotFoundException(participantId);
        }

        return toResponse(p);
    }

    @Transactional
    public ParticipantResponse updateStatus(Long participantId, ParticipantStatusRequest request, Long userId, String role) {
        Participant p = participantRepository.findById(participantId)
                .orElseThrow(() -> new ParticipantNotFoundException(participantId));

        String newStatus = request.getStatus().toUpperCase();

        if (newStatus.equals("CHECKED_IN")) {
            if (!role.equals("ORGANIZER")) {
                throw new UnauthorizedException("Only organizers can check in participants");
            }
            if (!p.getEvent().getOrganizer().getId().equals(userId)) {
                throw new UnauthorizedException("You can only check in participants for your own events");
            }
            p.setStatus(ParticipantStatus.CHECKED_IN);
            p.setCheckedInAt(LocalDateTime.now());

        } else if (newStatus.equals("CANCELLED")) {
            if (role.equals("ATTENDEE")) {
                boolean ownsParticipant = registrationRepository
                        .existsByUserIdAndRegistrationItemId(userId, p.getRegistrationItem().getId());
                if (!ownsParticipant) {
                    throw new UnauthorizedException("You can only cancel your own participants");
                }
            }
            p.setStatus(ParticipantStatus.CANCELLED);
            initiateRefundForParticipant(p);

        } else {
            throw new IllegalArgumentException("Invalid status: " + request.getStatus());
        }

        return toResponse(participantRepository.save(p));
    }

    @Transactional
    public ParticipantResponse updateParticipant(Long participantId, ParticipantUpdateRequest request, Long userId) {
        Participant p = participantRepository.findById(participantId)
                .orElseThrow(() -> new ParticipantNotFoundException(participantId));

        boolean ownsParticipant = registrationRepository
                .existsByUserIdAndRegistrationItemId(userId, p.getRegistrationItem().getId());
        if (!ownsParticipant) {
            throw new UnauthorizedException("You can only update your own participants");
        }

        if (p.getEvent().getStartTime() != null && LocalDateTime.now().isAfter(p.getEvent().getStartTime())) {
            throw new IllegalStateException("Cannot update participant after event has started");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            p.setName(request.getName().trim());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            p.setPhone(request.getPhone().trim());
        }

        return toResponse(participantRepository.save(p));
    }

    private void initiateRefundForParticipant(Participant participant) {
        boolean alreadyExists = refundRepository.findByParticipantIdAndStatus(
                participant.getId(), Refund.RefundStatus.PENDING).isPresent();

        if (alreadyExists) return;

        BigDecimal amountPaid = participant.getRegistrationItem().getPricePerTicket();

        Refund refund = new Refund();
        refund.setParticipant(participant);
        refund.setAmountPaid(amountPaid);
        refund.setRefundAmount(amountPaid);
        refund.setFeeAmount(BigDecimal.ZERO);
        refund.setStatus(Refund.RefundStatus.PENDING);
        refund.setCreatedAt(LocalDateTime.now());
        refundRepository.save(refund);
    }

    private ParticipantResponse toResponse(Participant p) {
        ParticipantResponse res = new ParticipantResponse();
        res.setId(p.getId());
        res.setName(p.getName());
        res.setEmail(p.getEmail());
        res.setPhone(p.getPhone());
        res.setTicketCode(p.getTicketCode());
        res.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
        res.setCheckedInAt(p.getCheckedInAt());
        res.setCreatedAt(p.getCreatedAt());
        res.setGender(p.getGender() != null ? p.getGender().name() : null);
        if (p.getEvent() != null) res.setEventId(p.getEvent().getId());
        return res;
    }
}

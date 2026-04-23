package com.project.ems.participant.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.project.ems.booking.repository.RegistrationItemRepository;
import com.project.ems.booking.repository.RegistrationRepository;
import com.project.ems.common.entity.Registration;
import com.project.ems.common.entity.RegistrationItem;
import com.project.ems.common.exception.RegistrationNotFoundException;
import com.project.ems.participant.dto.ParticipantCreateRequest;
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
    private final RegistrationItemRepository registrationItemRepository;
    private final RefundRepository refundRepository;

    public ParticipantService(ParticipantRepository participantRepository,
                              EventRepository eventRepository,
                              RegistrationRepository registrationRepository,
                              RegistrationItemRepository registrationItemRepository,
                              RefundRepository refundRepository) {
        this.participantRepository = participantRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.registrationItemRepository = registrationItemRepository;
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

    @Transactional
    public List<ParticipantResponse> createParticipants(List<ParticipantCreateRequest> requests, Long userId) {
        List<ParticipantResponse> responses = new ArrayList<>();

        for (ParticipantCreateRequest req : requests) {
            String email = req.getEmail().trim();
            String phone = req.getPhone().trim();
            Long eventId = req.getEventId();

            RegistrationItem item = registrationItemRepository.findById(req.getRegistrationItemId())
                    .orElseThrow(() -> new RegistrationNotFoundException(req.getRegistrationItemId()));

            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EventNotFoundException(eventId));

            if (!item.getRegistration().getUser().getId().equals(userId)) {
                throw new UnauthorizedException("You can only add participants to your own bookings");
            }

            if (!item.getRegistration().getEvent().getId().equals(eventId)) {
                throw new IllegalArgumentException("Registration item does not belong to the specified event");
            }

            // --- COLLISION CLEANUP LOGIC ---
            // 1. Check Email Collision
            participantRepository.findByEventIdAndEmail(eventId, email).ifPresent(p -> {
                if (p.getRegistrationItem().getRegistration().getStatus() == Registration.RegistrationStatus.CONFIRMED) {
                    throw new IllegalStateException("Email " + email + " is already registered for this event.");
                } else {
                    // It's a non-confirmed booking (PENDING, EXPIRED, etc.), so we clear it to make room.
                    participantRepository.delete(p);
                    participantRepository.flush(); // Force delete before the next insert
                }
            });

            // 2. Check Phone Collision
            participantRepository.findByEventIdAndPhone(eventId, phone).ifPresent(p -> {
                if (p.getRegistrationItem().getRegistration().getStatus() == Registration.RegistrationStatus.CONFIRMED) {
                    throw new IllegalStateException("Phone " + phone + " is already registered for this event.");
                } else {
                    participantRepository.delete(p);
                    participantRepository.flush();
                }
            });

            long existingCount = participantRepository.countByRegistrationItemId(item.getId());
            if (existingCount >= item.getQuantity()) {
                throw new IllegalStateException("Maximum participants already added for this registration item: " + item.getTicket().getName());
            }

            Participant p = new Participant();
            p.setRegistrationItem(item);
            p.setEvent(event);
            p.setName(req.getName().trim());
            p.setEmail(email);
            p.setPhone(phone);
            p.setGender(req.getGender());
            p.setTicketCode(UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
            p.setStatus(ParticipantStatus.ACTIVE);
            p.setCreatedAt(LocalDateTime.now());

            responses.add(toResponse(participantRepository.saveAndFlush(p)));
        }

        return responses;
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

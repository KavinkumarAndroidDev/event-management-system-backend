package com.project.ems.booking.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.booking.dto.BookingDetailResponse;
import com.project.ems.booking.dto.BookingItemResponse;
import com.project.ems.booking.repository.RegistrationItemRepository;
import com.project.ems.booking.repository.RegistrationRepository;
import com.project.ems.common.entity.Event;
import com.project.ems.common.entity.Registration;
import com.project.ems.common.entity.Registration.RegistrationStatus;
import com.project.ems.common.entity.RegistrationItem;
import com.project.ems.common.exception.EventNotFoundException;
import com.project.ems.common.exception.RegistrationNotFoundException;
import com.project.ems.common.exception.UnauthorizedException;
import com.project.ems.event.repository.EventRepository;

import java.util.List;

@Service
public class EventRegistrationService {

    private final RegistrationRepository registrationRepository;
    private final RegistrationItemRepository registrationItemRepository;
    private final EventRepository eventRepository;

    public EventRegistrationService(RegistrationRepository registrationRepository,
                                    RegistrationItemRepository registrationItemRepository,
                                    EventRepository eventRepository) {
        this.registrationRepository = registrationRepository;
        this.registrationItemRepository = registrationItemRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public Page<BookingDetailResponse> getRegistrationsForEvent(Long eventId, String status, int page, int size, Long userId, String role) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        if (role.equals("ORGANIZER") && !event.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedException("You can only view registrations for your own events");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (status != null && !status.isBlank()) {
            RegistrationStatus rs = RegistrationStatus.valueOf(status.toUpperCase());
            return registrationRepository.findByEventIdAndStatus(eventId, rs, pageable).map(this::toDetailResponse);
        }

        return registrationRepository.findByEventId(eventId, pageable).map(this::toDetailResponse);
    }

    @Transactional(readOnly = true)
    public BookingDetailResponse getRegistrationById(Long eventId, Long registrationId, Long userId, String role) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        if (role.equals("ORGANIZER") && !event.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedException("You can only view registrations for your own events");
        }

        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException(registrationId));

        if (!reg.getEvent().getId().equals(eventId)) {
            throw new RegistrationNotFoundException(registrationId);
        }

        return toDetailResponse(reg);
    }

    private BookingDetailResponse toDetailResponse(Registration reg) {
        BookingDetailResponse res = new BookingDetailResponse();
        res.setId(reg.getId());
        res.setStatus(reg.getStatus().name());
        res.setTotalAmount(reg.getTotalAmount());
        res.setCreatedAt(reg.getCreatedAt());

        if (reg.getEvent() != null) {
            res.setEventId(reg.getEvent().getId());
            res.setEventTitle(reg.getEvent().getTitle());
        }

        List<RegistrationItem> items = registrationItemRepository.findByRegistrationId(reg.getId());
        res.setItems(items.stream().map(item -> {
            BookingItemResponse ir = new BookingItemResponse();
            ir.setId(item.getId());
            ir.setQuantity(item.getQuantity());
            ir.setPricePerTicket(item.getPricePerTicket());
            if (item.getTicket() != null) {
                ir.setTicketId(item.getTicket().getId());
                ir.setTicketName(item.getTicket().getName());
            }
            return ir;
        }).toList());

        return res;
    }
}

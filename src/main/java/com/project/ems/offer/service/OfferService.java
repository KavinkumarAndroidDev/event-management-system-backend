package com.project.ems.offer.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.common.entity.Event;
import com.project.ems.common.entity.Offer;
import com.project.ems.common.exception.EventNotFoundException;
import com.project.ems.common.exception.OfferAlreadyExistsException;
import com.project.ems.common.exception.OfferNotFoundException;
import com.project.ems.common.exception.UnauthorizedException;
import com.project.ems.event.repository.EventRepository;
import com.project.ems.offer.dto.OfferCreateRequest;
import com.project.ems.offer.dto.OfferResponse;
import com.project.ems.offer.dto.OfferUpdateRequest;
import com.project.ems.offer.dto.OfferValidateRequest;
import com.project.ems.offer.dto.OfferStatusRequest;
import com.project.ems.offer.dto.OfferValidateResponse;
import com.project.ems.offer.repository.OfferRepository;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final EventRepository eventRepository;

    public OfferService(OfferRepository offerRepository, EventRepository eventRepository) {
        this.offerRepository = offerRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<OfferResponse> getOffersForEvent(Long eventId, Long userId, String role) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        if (role.equals("ORGANIZER") && !event.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedException("You can only view offers for your own events");
        }

        return offerRepository.findByEventId(eventId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public OfferResponse createOffer(Long eventId, OfferCreateRequest request, Long userId, String role) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (role.equals("ORGANIZER") && !event.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedException("You can only create offers for your own events");
        }

        boolean alreadyExists = offerRepository.findByEventIdAndCode(eventId, request.getCode().toUpperCase()).isPresent();
        if (alreadyExists) {
            throw new OfferAlreadyExistsException("Offer code already exists for this event");
        }

        Offer offer = new Offer();
        offer.setEvent(event);
        offer.setCode(request.getCode().toUpperCase().trim());
        offer.setDiscountPercentage(request.getDiscountPercentage());
        offer.setMaxDiscountAmount(request.getMaxDiscountAmount());
        offer.setValidFrom(request.getValidFrom());
        offer.setValidTo(request.getValidTo());
        offer.setTotalUsageLimit(request.getTotalUsageLimit());

        return toResponse(offerRepository.save(offer));
    }

    @Transactional
    public OfferResponse updateOffer(Long offerId, OfferUpdateRequest request, Long userId, String role) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new OfferNotFoundException(offerId));

        if (role.equals("ORGANIZER") && !offer.getEvent().getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedException("You can only update offers for your own events");
        }

        if (request.getValidFrom() != null) offer.setValidFrom(request.getValidFrom());
        if (request.getValidTo() != null) offer.setValidTo(request.getValidTo());
        if (request.getTotalUsageLimit() != null) offer.setTotalUsageLimit(request.getTotalUsageLimit());

        return toResponse(offerRepository.save(offer));
    }

    @Transactional(readOnly = true)
    public OfferValidateResponse validateOffer(OfferValidateRequest request) {
        Offer offer = offerRepository.findByEventIdAndCode(request.getEventId(), request.getCode().toUpperCase())
                .orElseThrow(() -> new OfferNotFoundException("Invalid offer code"));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(offer.getValidFrom()) || now.isAfter(offer.getValidTo())) {
            throw new OfferNotFoundException("Offer code has expired or is not yet active");
        }

        BigDecimal discountAmount = request.getAmount()
                .multiply(offer.getDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        if (offer.getMaxDiscountAmount() != null && discountAmount.compareTo(offer.getMaxDiscountAmount()) > 0) {
            discountAmount = offer.getMaxDiscountAmount();
        }

        BigDecimal finalAmount = request.getAmount().subtract(discountAmount);

        OfferValidateResponse response = new OfferValidateResponse();
        response.setCode(offer.getCode());
        response.setDiscountPercentage(offer.getDiscountPercentage());
        response.setDiscountAmount(discountAmount);
        response.setFinalAmount(finalAmount.max(BigDecimal.ZERO));

        return response;
    }

    private OfferResponse toResponse(Offer offer) {
        OfferResponse res = new OfferResponse();
        res.setId(offer.getId());
        res.setCode(offer.getCode());
        res.setDiscountPercentage(offer.getDiscountPercentage());
        res.setMaxDiscountAmount(offer.getMaxDiscountAmount());
        res.setValidFrom(offer.getValidFrom());
        res.setValidTo(offer.getValidTo());
        res.setTotalUsageLimit(offer.getTotalUsageLimit());
        if (offer.getEvent() != null) {
            res.setEventId(offer.getEvent().getId());
        }
        return res;
    }

    @Transactional
    public OfferResponse updateOfferStatus(Long offerId, OfferStatusRequest request, Long userId, String role) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new OfferNotFoundException(offerId));

        if (role.equals("ORGANIZER") && !offer.getEvent().getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedException("You can only manage offers for your own events");
        }

        String status = request.getStatus().toUpperCase();
        if (status.equals("INACTIVE")) {
            offer.setValidTo(LocalDateTime.now().minusSeconds(1));
        } else if (status.equals("ACTIVE")) {
            if (offer.getValidTo().isBefore(LocalDateTime.now())) {
                offer.setValidTo(LocalDateTime.now().plusDays(30));
            }
        } else {
            throw new IllegalArgumentException("Status must be ACTIVE or INACTIVE");
        }

        return toResponse(offerRepository.save(offer));
    }

}
package com.project.ems.refund.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.common.entity.Event;
import com.project.ems.common.entity.Refund;
import com.project.ems.common.entity.Refund.RefundStatus;
import com.project.ems.common.exception.EventNotFoundException;
import com.project.ems.common.exception.RefundNotFoundException;
import com.project.ems.common.exception.UnauthorizedException;
import com.project.ems.event.repository.EventRepository;
import com.project.ems.refund.dto.RefundResponse;
import com.project.ems.refund.repository.RefundRepository;

@Service
public class RefundService {

    private final RefundRepository refundRepository;
    private final EventRepository eventRepository;

    public RefundService(RefundRepository refundRepository, EventRepository eventRepository) {
        this.refundRepository = refundRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public Page<RefundResponse> getRefundsForEvent(Long eventId, Long userId, int page, int size) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedException("You can only view refunds for your own events");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return refundRepository.findByEventId(eventId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<RefundResponse> getAllRefunds(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (status != null && !status.isBlank()) {
            RefundStatus rs = RefundStatus.valueOf(status.toUpperCase());
            return refundRepository.findByStatus(rs, pageable).map(this::toResponse);
        }

        return refundRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public RefundResponse retryRefund(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundNotFoundException(refundId));

        if (refund.getStatus() != RefundStatus.FAILED) {
            throw new IllegalStateException("Only FAILED refunds can be retried");
        }

        refund.setStatus(RefundStatus.PENDING);
        return toResponse(refundRepository.save(refund));
    }

    private RefundResponse toResponse(Refund r) {
        RefundResponse res = new RefundResponse();
        res.setId(r.getId());
        res.setAmountPaid(r.getAmountPaid());
        res.setRefundAmount(r.getRefundAmount());
        res.setFeeAmount(r.getFeeAmount());
        res.setStatus(r.getStatus() != null ? r.getStatus().name() : null);
        res.setGatewayRefundId(r.getGatewayRefundId());
        res.setRefundedAt(r.getRefundedAt());
        res.setCreatedAt(r.getCreatedAt());
        if (r.getParticipant() != null) {
            res.setParticipantId(r.getParticipant().getId());
            res.setParticipantName(r.getParticipant().getName());
        }
        return res;
    }
}

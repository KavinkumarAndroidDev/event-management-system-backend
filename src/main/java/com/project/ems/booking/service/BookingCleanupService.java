package com.project.ems.booking.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.booking.repository.RegistrationItemRepository;
import com.project.ems.booking.repository.RegistrationRepository;
import com.project.ems.common.entity.Payment;
import com.project.ems.common.entity.Registration;
import com.project.ems.common.entity.Registration.RegistrationStatus;
import com.project.ems.common.entity.RegistrationItem;
import com.project.ems.common.entity.Ticket;
import com.project.ems.participant.repository.ParticipantRepository;
import com.project.ems.payment.repository.PaymentRepository;
import com.project.ems.ticket.repository.TicketRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BookingCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(BookingCleanupService.class);

    private final RegistrationRepository registrationRepository;
    private final RegistrationItemRepository registrationItemRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final ParticipantRepository participantRepository;

    public BookingCleanupService(RegistrationRepository registrationRepository,
                                 RegistrationItemRepository registrationItemRepository,
                                 TicketRepository ticketRepository,
                                 PaymentRepository paymentRepository, ParticipantRepository participantRepository) {
        this.registrationRepository = registrationRepository;
        this.registrationItemRepository = registrationItemRepository;
        this.ticketRepository = ticketRepository;
        this.paymentRepository = paymentRepository;
        this.participantRepository = participantRepository;
    }

    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void cleanupExpiredBookings() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
        List<Registration> staleBookings = registrationRepository.findByStatusAndCreatedAtBefore(RegistrationStatus.PENDING, threshold);

        if (staleBookings.isEmpty()) {
            return;
        }

        logger.info("Found {} stale PENDING bookings to expire", staleBookings.size());

        for (Registration reg : staleBookings) {
            expireBooking(reg);
        }
    }

    private void expireBooking(Registration reg) {
        logger.info("Expiring booking ID: {} created at: {}", reg.getId(), reg.getCreatedAt());
        
        List<RegistrationItem> items = registrationItemRepository.findByRegistrationId(reg.getId());
        if (!reg.isStockReleased()) {
            for (RegistrationItem item : items) {
                ticketRepository.restoreAvailableQuantity(item.getTicket().getId(), item.getQuantity());
            }
            reg.setStockReleased(true);
        }

        // 2. Delete associated participants (to free up unique constraints)
        participantRepository.deleteByRegistrationItemRegistrationId(reg.getId());

        // 3. Mark registration as EXPIRED
        reg.setStatus(RegistrationStatus.EXPIRED);
        registrationRepository.save(reg);

        // 4. Mark associated payment as FAILED if it exists
        paymentRepository.findByRegistrationId(reg.getId()).ifPresent(payment -> {
            if (payment.getStatus() == Payment.PaymentStatus.PENDING) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }
        });
    }
}

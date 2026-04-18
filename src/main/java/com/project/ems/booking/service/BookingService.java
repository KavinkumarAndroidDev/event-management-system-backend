package com.project.ems.booking.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.auth.repository.UserRepository;
import com.project.ems.booking.dto.BookingCreateRequest;
import com.project.ems.booking.dto.BookingCreateResponse;
import com.project.ems.booking.dto.BookingDetailResponse;
import com.project.ems.booking.dto.BookingItemRequest;
import com.project.ems.booking.dto.BookingItemResponse;
import com.project.ems.booking.dto.BookingPreviewRequest;
import com.project.ems.booking.dto.BookingPreviewResponse;
import com.project.ems.booking.dto.BookingStatusRequest;
import com.project.ems.booking.repository.RegistrationItemRepository;
import com.project.ems.booking.repository.RegistrationRepository;
import com.project.ems.common.entity.Event;
import com.project.ems.common.entity.Participant;
import com.project.ems.common.entity.Payment;
import com.project.ems.common.entity.Registration;
import com.project.ems.common.entity.Registration.RegistrationStatus;
import com.project.ems.common.entity.RegistrationItem;
import com.project.ems.common.entity.Status;
import com.project.ems.common.entity.Ticket;
import com.project.ems.common.entity.User;
import com.project.ems.common.exception.EventNotFoundException;
import com.project.ems.common.exception.RegistrationNotFoundException;
import com.project.ems.common.exception.RazorpayOrderException;
import com.project.ems.common.exception.TicketNotFoundException;
import com.project.ems.common.exception.UnauthorizedException;
import com.project.ems.common.exception.UserNotFoundException;
import com.project.ems.event.repository.EventRepository;
import com.project.ems.offer.repository.OfferRepository;
import com.project.ems.participant.repository.ParticipantRepository;
import com.project.ems.payment.repository.PaymentRepository;
import com.project.ems.refund.dto.RefundResponse;
import com.project.ems.refund.repository.RefundRepository;
import com.project.ems.ticket.repository.TicketRepository;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;

@Service
public class BookingService {

    private final RegistrationRepository registrationRepository;
    private final RegistrationItemRepository registrationItemRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final OfferRepository offerRepository;
    private final RefundRepository refundRepository;
    private final ParticipantRepository participantRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public BookingService(RegistrationRepository registrationRepository,
                          RegistrationItemRepository registrationItemRepository,
                          EventRepository eventRepository,
                          TicketRepository ticketRepository,
                          UserRepository userRepository,
                          PaymentRepository paymentRepository,
                          OfferRepository offerRepository,
                          RefundRepository refundRepository,
                          ParticipantRepository participantRepository) {
        this.registrationRepository = registrationRepository;
        this.registrationItemRepository = registrationItemRepository;
        this.eventRepository = eventRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.offerRepository = offerRepository;
        this.refundRepository = refundRepository;
        this.participantRepository = participantRepository;
    }

    @Transactional(readOnly = true)
    public BookingPreviewResponse preview(BookingPreviewRequest request, Long userId) {
        eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new EventNotFoundException(request.getEventId()));

        List<BookingPreviewResponse.BookingItemSummary> summaries = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (BookingItemRequest item : request.getItems()) {
            Ticket ticket = ticketRepository.findById(item.getTicketId())
                    .orElseThrow(() -> new TicketNotFoundException(item.getTicketId()));

            if (!ticket.getEvent().getId().equals(request.getEventId())) {
                throw new TicketNotFoundException(item.getTicketId());
            }

            BigDecimal lineTotal = ticket.getPrice().multiply(BigDecimal.valueOf(item.getQty()));
            subtotal = subtotal.add(lineTotal);

            BookingPreviewResponse.BookingItemSummary summary = new BookingPreviewResponse.BookingItemSummary();
            summary.setTicketId(ticket.getId());
            summary.setTicketName(ticket.getName());
            summary.setQty(item.getQty());
            summary.setPricePerTicket(ticket.getPrice());
            summary.setLineTotal(lineTotal);
            summaries.add(summary);
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (request.getOfferCode() != null && !request.getOfferCode().isBlank()) {
            discount = calculateDiscount(request.getEventId(), request.getOfferCode(), subtotal);
        }

        BookingPreviewResponse response = new BookingPreviewResponse();
        response.setEventId(request.getEventId());
        response.setItems(summaries);
        response.setSubtotal(subtotal);
        response.setDiscountAmount(discount);
        response.setTotalAmount(subtotal.subtract(discount).max(BigDecimal.ZERO));
        response.setOfferCode(request.getOfferCode());
        return response;
    }

    @Transactional
    public BookingCreateResponse createBooking(BookingCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new EventNotFoundException(request.getEventId()));

        if (event.getStatus() != Event.EventStatus.PUBLISHED) {
            throw new IllegalStateException("Event is not available for booking");
        }
//
//        if (registrationRepository.existsActiveBookingByUserIdAndEventId(userId, event.getId())) {
//            throw new IllegalStateException("You already have an active booking for this event");
//        }

        int totalQty = request.getItems().stream().mapToInt(BookingItemRequest::getQty).sum();
        if (totalQty > 10) {
            throw new IllegalStateException("Cannot book more than 10 tickets in a single order");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<RegistrationItem> itemsToSave = new ArrayList<>();

        Registration registration = new Registration();
        registration.setUser(user);
        registration.setEvent(event);
        registration.setStatus(RegistrationStatus.PENDING);
        registration.setCreatedAt(LocalDateTime.now());

        Registration savedRegistration = registrationRepository.save(registration);

        LocalDateTime now = LocalDateTime.now();

        for (BookingItemRequest itemReq : request.getItems()) {
            Ticket ticket = ticketRepository.findById(itemReq.getTicketId())
                    .orElseThrow(() -> new TicketNotFoundException(itemReq.getTicketId()));

            if (!ticket.getEvent().getId().equals(event.getId())) {
                throw new TicketNotFoundException(itemReq.getTicketId());
            }

            if (ticket.getStatus() != Status.ACTIVE) {
                throw new IllegalStateException("Ticket is not available for sale: " + ticket.getName());
            }

            if (ticket.getSaleStartTime() != null && now.isBefore(ticket.getSaleStartTime())) {
                throw new IllegalStateException("Ticket sale has not started yet: " + ticket.getName());
            }

            if (ticket.getSaleEndTime() != null && now.isAfter(ticket.getSaleEndTime())) {
                throw new IllegalStateException("Ticket sale has ended: " + ticket.getName());
            }

            if (ticket.getAvailableQuantity() < itemReq.getQty()) {
                throw new IllegalStateException("Not enough tickets available for: " + ticket.getName());
            }

            ticket.setAvailableQuantity(ticket.getAvailableQuantity() - itemReq.getQty());
            ticketRepository.save(ticket);

            RegistrationItem item = new RegistrationItem();
            item.setRegistration(savedRegistration);
            item.setTicket(ticket);
            item.setQuantity(itemReq.getQty());
            item.setPricePerTicket(ticket.getPrice());
            itemsToSave.add(registrationItemRepository.save(item));

            subtotal = subtotal.add(ticket.getPrice().multiply(BigDecimal.valueOf(itemReq.getQty())));
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (request.getOfferCode() != null && !request.getOfferCode().isBlank()) {
            discount = calculateDiscount(event.getId(), request.getOfferCode(), subtotal);
        }

        BigDecimal total = subtotal.subtract(discount).max(BigDecimal.ZERO);
        savedRegistration.setTotalAmount(total);
        registrationRepository.save(savedRegistration);

        String razorpayOrderId = createRazorpayOrder(total, savedRegistration.getId());

        Payment payment = new Payment();
        payment.setRegistration(savedRegistration);
        payment.setGateway("razorpay");
        payment.setRazorpayOrderId(razorpayOrderId);
        payment.setAmount(total);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        BookingCreateResponse response = new BookingCreateResponse();
        response.setBookingId(savedRegistration.getId());
        response.setRazorpayOrderId(razorpayOrderId);
        response.setAmount(total);
        response.setCurrency("INR");
        return response;
    }

    @Transactional(readOnly = true)
    public Page<BookingDetailResponse> getMyBookings(Long userId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (status != null && !status.isBlank()) {
            RegistrationStatus rs = RegistrationStatus.valueOf(status.toUpperCase());
            return registrationRepository.findByUserIdAndStatus(userId, rs, pageable).map(this::toDetailResponse);
        }

        return registrationRepository.findByUserId(userId, pageable).map(this::toDetailResponse);
    }

    @Transactional(readOnly = true)
    public BookingDetailResponse getBookingById(Long bookingId, Long userId) {
        Registration reg = registrationRepository.findById(bookingId)
                .orElseThrow(() -> new RegistrationNotFoundException(bookingId));

        if (!reg.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("This booking does not belong to you");
        }

        return toDetailResponse(reg);
    }

    @Transactional(readOnly = true)
    public BookingDetailResponse getPendingBookingForEvent(Long eventId, Long userId) {
        return registrationRepository.findPendingBookingByUserIdAndEventId(userId, eventId)
                .map(this::toDetailResponse)
                .orElse(null);
    }

    @Transactional
    public BookingDetailResponse cancelBooking(Long bookingId, BookingStatusRequest request, Long userId) {
        Registration reg = registrationRepository.findById(bookingId)
                .orElseThrow(() -> new RegistrationNotFoundException(bookingId));

        if (!reg.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("This booking does not belong to you");
        }

        if (!request.getStatus().equalsIgnoreCase("CANCELLED")) {
            throw new IllegalArgumentException("Only CANCELLED status is allowed here");
        }

        if (reg.getStatus() != RegistrationStatus.CONFIRMED) {
            throw new IllegalStateException("Only CONFIRMED bookings can be cancelled");
        }

        Event event = reg.getEvent();
        if (!Boolean.TRUE.equals(event.getIsCancellable())) {
            throw new IllegalStateException("This event does not allow cancellations");
        }

        if (event.getCancellationDeadline() != null && LocalDateTime.now().isAfter(event.getCancellationDeadline())) {
            throw new IllegalStateException("Cancellation deadline has passed");
        }

        reg.setStatus(RegistrationStatus.CANCELLED);
        registrationRepository.save(reg);

        List<RegistrationItem> items = registrationItemRepository.findByRegistrationId(reg.getId());
        for (RegistrationItem item : items) {
            Ticket ticket = item.getTicket();
            ticket.setAvailableQuantity(ticket.getAvailableQuantity() + item.getQuantity());
            ticketRepository.save(ticket);

            List<Participant> participants = participantRepository.findByRegistrationItemId(item.getId());
            for (Participant p : participants) {
                if (p.getStatus() == Participant.ParticipantStatus.ACTIVE) {
                    p.setStatus(Participant.ParticipantStatus.CANCELLED);
                    participantRepository.save(p);
                }
            }
        }

        return toDetailResponse(reg);
    }

    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundsForBooking(Long bookingId, Long userId) {
        Registration reg = registrationRepository.findById(bookingId)
                .orElseThrow(() -> new RegistrationNotFoundException(bookingId));

        if (!reg.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("This booking does not belong to you");
        }

        return refundRepository.findByRegistrationId(bookingId).stream().map(r -> {
            RefundResponse res = new RefundResponse();
            res.setId(r.getId());
            res.setParticipantId(r.getParticipant().getId());
            res.setParticipantName(r.getParticipant().getName());
            res.setAmountPaid(r.getAmountPaid());
            res.setRefundAmount(r.getRefundAmount());
            res.setFeeAmount(r.getFeeAmount());
            res.setStatus(r.getStatus().name());
            res.setGatewayRefundId(r.getGatewayRefundId());
            res.setRefundedAt(r.getRefundedAt());
            res.setCreatedAt(r.getCreatedAt());
            return res;
        }).toList();
    }

    private BigDecimal calculateDiscount(Long eventId, String offerCode, BigDecimal subtotal) {
        return offerRepository.findByEventIdAndCode(eventId, offerCode.toUpperCase()).map(offer -> {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(offer.getValidFrom()) || now.isAfter(offer.getValidTo())) {
                return BigDecimal.ZERO;
            }
            BigDecimal disc = subtotal.multiply(offer.getDiscountPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (offer.getMaxDiscountAmount() != null && disc.compareTo(offer.getMaxDiscountAmount()) > 0) {
                disc = offer.getMaxDiscountAmount();
            }
            return disc;
        }).orElse(BigDecimal.ZERO);
    }

    private String createRazorpayOrder(BigDecimal amount, Long registrationId) {
        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject options = new JSONObject();
            options.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
            options.put("currency", "INR");
            options.put("receipt", "booking_" + registrationId);
            Order order = client.orders.create(options);
            return order.get("id");
        } catch (Exception e) {
            throw new RazorpayOrderException("Failed to create Razorpay order: " + e.getMessage());
        }
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
            res.setEventStartTime(reg.getEvent().getStartTime()); 
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
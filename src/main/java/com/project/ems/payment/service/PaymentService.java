package com.project.ems.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.booking.repository.RegistrationItemRepository;
import com.project.ems.booking.repository.RegistrationRepository;
import com.project.ems.common.entity.Participant;
import com.project.ems.common.entity.Payment;
import com.project.ems.common.entity.Payment.PaymentStatus;
import com.project.ems.common.entity.Registration;
import com.project.ems.common.entity.Registration.RegistrationStatus;
import com.project.ems.common.entity.RegistrationItem;
import com.project.ems.common.exception.PaymentNotFoundException;
import com.project.ems.common.exception.PaymentVerificationException;
import com.project.ems.common.exception.RazorpayOrderException;
import com.project.ems.common.exception.RegistrationNotFoundException;
import com.project.ems.common.exception.UnauthorizedException;
import com.project.ems.payment.dto.PaymentFailRequest;
import com.project.ems.payment.dto.PaymentResponse;
import com.project.ems.payment.dto.PaymentRetryRequest;
import com.project.ems.payment.dto.PaymentVerifyRequest;
import com.project.ems.payment.repository.PaymentRepository;
import com.project.ems.participant.repository.ParticipantRepository;

import com.razorpay.RazorpayClient;
import com.razorpay.Order;

import org.json.JSONObject;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RegistrationRepository registrationRepository;
    private final RegistrationItemRepository registrationItemRepository;
    private final ParticipantRepository participantRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public PaymentService(PaymentRepository paymentRepository,
                          RegistrationRepository registrationRepository,
                          RegistrationItemRepository registrationItemRepository,
                          ParticipantRepository participantRepository) {
        this.paymentRepository = paymentRepository;
        this.registrationRepository = registrationRepository;
        this.registrationItemRepository = registrationItemRepository;
        this.participantRepository = participantRepository;
    }

    @Transactional
    public PaymentResponse verifyPayment(PaymentVerifyRequest request, Long userId) {
        Registration reg = registrationRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RegistrationNotFoundException(request.getBookingId()));

        if (!reg.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("This booking does not belong to you");
        }

        Payment payment = paymentRepository.findByRegistrationId(reg.getId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment record not found"));

        if (!verifySignature(request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature())) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            reg.setStatus(RegistrationStatus.FAILED);
            registrationRepository.save(reg);
            throw new PaymentVerificationException("Payment signature verification failed");
        }

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        reg.setStatus(RegistrationStatus.CONFIRMED);
        registrationRepository.save(reg);

        createParticipants(reg);

        return toResponse(payment);
    }

    @Transactional
    public void markFailed(PaymentFailRequest request, Long userId) {
        Registration reg = registrationRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RegistrationNotFoundException(request.getBookingId()));

        if (!reg.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("This booking does not belong to you");
        }

        Payment payment = paymentRepository.findByRegistrationId(reg.getId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        reg.setStatus(RegistrationStatus.FAILED);
        registrationRepository.save(reg);
    }

    @Transactional
    public String retryPayment(PaymentRetryRequest request, Long userId) {
        Registration reg = registrationRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RegistrationNotFoundException(request.getBookingId()));

        if (!reg.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("This booking does not belong to you");
        }

        if (reg.getStatus() != RegistrationStatus.FAILED) {
            throw new IllegalStateException("Only FAILED bookings can be retried");
        }

        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject options = new JSONObject();
            options.put("amount", reg.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue());
            options.put("currency", "INR");
            options.put("receipt", "retry_" + reg.getId());
            Order order = client.orders.create(options);
            String newOrderId = order.get("id");

            Payment payment = paymentRepository.findByRegistrationId(reg.getId())
                    .orElse(new Payment());
            payment.setRegistration(reg);
            payment.setGateway("razorpay");
            payment.setRazorpayOrderId(newOrderId);
            payment.setAmount(reg.getTotalAmount());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setCreatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            reg.setStatus(RegistrationStatus.PENDING);
            registrationRepository.save(reg);

            return newOrderId;
        } catch (Exception e) {
            throw new RazorpayOrderException("Failed to create retry order: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentForBooking(Long bookingId, Long userId) {
        Registration reg = registrationRepository.findById(bookingId)
                .orElseThrow(() -> new RegistrationNotFoundException(bookingId));

        if (!reg.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("This booking does not belong to you");
        }

        Payment payment = paymentRepository.findByRegistrationId(bookingId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (status != null && !status.isBlank()) {
            PaymentStatus ps = PaymentStatus.valueOf(status.toUpperCase());
            return paymentRepository.findByStatus(ps, pageable).map(this::toResponse);
        }

        return paymentRepository.findAll(pageable).map(this::toResponse);
    }

    private void createParticipants(Registration reg) {
        for (RegistrationItem item : registrationItemRepository.findByRegistrationId(reg.getId())) {
            for (int i = 0; i < item.getQuantity(); i++) {
                Participant p = new Participant();
                p.setRegistrationItem(item);
                p.setEvent(reg.getEvent());
                p.setName(reg.getUser().getFullName() != null ? reg.getUser().getFullName() : reg.getUser().getEmail());
                p.setEmail(reg.getUser().getEmail());
                p.setPhone(reg.getUser().getPhone() != null ? reg.getUser().getPhone() : "0000000000");
                p.setTicketCode(UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
                p.setStatus(Participant.ParticipantStatus.ACTIVE);
                p.setCreatedAt(LocalDateTime.now());
                participantRepository.save(p);
            }
        }
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpayKeySecret.getBytes(), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private PaymentResponse toResponse(Payment p) {
        PaymentResponse res = new PaymentResponse();
        res.setId(p.getId());
        res.setGateway(p.getGateway());
        res.setRazorpayOrderId(p.getRazorpayOrderId());
        res.setRazorpayPaymentId(p.getRazorpayPaymentId());
        res.setAmount(p.getAmount());
        res.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
        res.setPaymentMode(p.getPaymentMode() != null ? p.getPaymentMode().name() : null);
        res.setCreatedAt(p.getCreatedAt());
        res.setPaidAt(p.getPaidAt());
        if (p.getRegistration() != null) {
            res.setRegistrationId(p.getRegistration().getId());
        }
        return res;
    }


}
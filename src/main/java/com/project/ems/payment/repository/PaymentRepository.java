package com.project.ems.payment.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.ems.common.entity.Payment;
import com.project.ems.common.entity.Payment.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRegistrationId(Long registrationId);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
}

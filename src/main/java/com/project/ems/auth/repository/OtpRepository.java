package com.project.ems.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.ems.common.entity.OtpVerification;

public interface OtpRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findTopByIdentifierAndUsedFalseOrderByCreatedAtDesc(String identifier);
}

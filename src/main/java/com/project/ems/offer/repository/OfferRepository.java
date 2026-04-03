package com.project.ems.offer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.ems.common.entity.Offer;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    List<Offer> findByEventId(Long eventId);

    Optional<Offer> findByEventIdAndCode(Long eventId, String code);
}

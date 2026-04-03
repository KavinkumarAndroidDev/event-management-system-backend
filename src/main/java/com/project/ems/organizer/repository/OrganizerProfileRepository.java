package com.project.ems.organizer.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.ems.common.entity.OrganizerProfile;

public interface OrganizerProfileRepository extends JpaRepository<OrganizerProfile, Long> {

    Optional<OrganizerProfile> findByUserId(Long userId);

    boolean existsByUserIdAndVerifiedTrue(Long userId);

    // for admin listing — filter by verified status with pagination
    Page<OrganizerProfile> findByVerified(Boolean verified, Pageable pageable);
}

package com.project.ems.booking.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.ems.common.entity.Registration;
import com.project.ems.common.entity.Registration.RegistrationStatus;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    Page<Registration> findByUserId(Long userId, Pageable pageable);

    Page<Registration> findByUserIdAndStatus(Long userId, RegistrationStatus status, Pageable pageable);

    Page<Registration> findByEventId(Long eventId, Pageable pageable);

    Page<Registration> findByEventIdAndStatus(Long eventId, RegistrationStatus status, Pageable pageable);

    List<Registration> findByEventId(Long eventId);

    @Query("SELECT COUNT(ri) > 0 FROM RegistrationItem ri JOIN ri.registration r WHERE r.user.id = :userId AND ri.id = :registrationItemId")
    boolean existsByUserIdAndRegistrationItemId(@Param("userId") Long userId, @Param("registrationItemId") Long registrationItemId);
}

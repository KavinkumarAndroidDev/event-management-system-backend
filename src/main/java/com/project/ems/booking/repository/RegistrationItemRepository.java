package com.project.ems.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.ems.common.entity.RegistrationItem;

public interface RegistrationItemRepository extends JpaRepository<RegistrationItem, Long> {

    List<RegistrationItem> findByRegistrationId(Long registrationId);
}

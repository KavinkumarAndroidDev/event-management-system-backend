package com.project.ems.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.ems.common.entity.BroadcastLog;

public interface BroadcastLogRepository extends JpaRepository<BroadcastLog, Long> {

    Page<BroadcastLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

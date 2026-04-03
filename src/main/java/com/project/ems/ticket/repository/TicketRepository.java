package com.project.ems.ticket.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.ems.common.entity.Status;
import com.project.ems.common.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByEventId(Long eventId);

    List<Ticket> findByEventIdAndStatus(Long eventId, Status status);
}

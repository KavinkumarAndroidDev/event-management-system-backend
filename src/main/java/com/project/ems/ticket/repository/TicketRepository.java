package com.project.ems.ticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.ems.common.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
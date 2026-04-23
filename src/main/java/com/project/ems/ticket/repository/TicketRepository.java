package com.project.ems.ticket.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.ems.common.entity.Status;
import com.project.ems.common.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByEventId(Long eventId);

    List<Ticket> findByEventIdAndStatus(Long eventId, Status status);

    @Modifying
    @Query("UPDATE Ticket t SET t.availableQuantity = t.availableQuantity - :qty " +
            "WHERE t.id = :ticketId AND t.event.id = :eventId AND t.availableQuantity >= :qty")
    int reduceAvailableQuantity(@Param("ticketId") Long ticketId,
                                @Param("eventId") Long eventId,
                                @Param("qty") int qty);

    @Modifying
    @Query("UPDATE Ticket t SET t.availableQuantity = t.availableQuantity + :qty WHERE t.id = :ticketId")
    int restoreAvailableQuantity(@Param("ticketId") Long ticketId, @Param("qty") int qty);
}

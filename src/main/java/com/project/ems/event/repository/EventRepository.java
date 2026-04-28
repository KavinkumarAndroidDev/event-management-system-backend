package com.project.ems.event.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.ems.common.entity.Event;
import com.project.ems.common.entity.Event.EventStatus;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatus(EventStatus status);

    List<Event> findByStatusAndStartTimeAfter(EventStatus status, LocalDateTime time);

    List<Event> findByOrganizerId(Long organizerId);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.organizer.id = :organizerId")
    Long countByOrganizerId(@Param("organizerId") Long organizerId);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.organizer.id = :organizerId AND e.status = :status")
    Long countByOrganizerIdAndStatus(@Param("organizerId") Long organizerId, @Param("status") EventStatus status);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.status = :status")
    Long countByStatus(@Param("status") EventStatus status);

    @Query("SELECT e FROM Event e WHERE " +
           "e.status = 'PUBLISHED' " +
           "AND e.startTime > :now " +
           "AND e.category.status = 'ACTIVE' " +
           "AND e.venue.status = 'ACTIVE' " +
           "AND (:search IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
           "AND (:city IS NULL OR LOWER(e.venue.city) = LOWER(:city)) " +
           "AND (:venueId IS NULL OR e.venue.id = :venueId) " +
           "AND (:date IS NULL OR CAST(e.startTime AS date) = CAST(:date AS date)) " +
           "AND ((:minPrice IS NULL AND :maxPrice IS NULL) OR EXISTS (SELECT t FROM Ticket t WHERE t.event = e AND t.status = 'ACTIVE' " +
           "AND (:minPrice IS NULL OR t.price >= :minPrice) AND (:maxPrice IS NULL OR t.price <= :maxPrice)))")
    Page<Event> findPublicEvents(
            @Param("now") LocalDateTime now,
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("city") String city,
            @Param("venueId") Long venueId,
            @Param("date") LocalDateTime date,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("SELECT e FROM Event e WHERE " +
           "e.organizer.id = :organizerId " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:search IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
           "AND (:city IS NULL OR LOWER(e.venue.city) = LOWER(:city)) " +
           "AND (:venueId IS NULL OR e.venue.id = :venueId) " +
           "AND (:date IS NULL OR CAST(e.startTime AS date) = CAST(:date AS date)) " +
           "AND ((:minPrice IS NULL AND :maxPrice IS NULL) OR EXISTS (SELECT t FROM Ticket t WHERE t.event = e AND t.status = 'ACTIVE' " +
           "AND (:minPrice IS NULL OR t.price >= :minPrice) AND (:maxPrice IS NULL OR t.price <= :maxPrice)))")
    Page<Event> findOrganizerEvents(
            @Param("organizerId") Long organizerId,
            @Param("status") EventStatus status,
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("city") String city,
            @Param("venueId") Long venueId,
            @Param("date") LocalDateTime date,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("SELECT e FROM Event e WHERE " +
           "(:status IS NULL OR e.status = :status) " +
           "AND (:search IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
           "AND (:city IS NULL OR LOWER(e.venue.city) = LOWER(:city)) " +
           "AND (:venueId IS NULL OR e.venue.id = :venueId) " +
           "AND (:date IS NULL OR CAST(e.startTime AS date) = CAST(:date AS date)) " +
           "AND ((:minPrice IS NULL AND :maxPrice IS NULL) OR EXISTS (SELECT t FROM Ticket t WHERE t.event = e AND t.status = 'ACTIVE' " +
           "AND (:minPrice IS NULL OR t.price >= :minPrice) AND (:maxPrice IS NULL OR t.price <= :maxPrice)))")
    Page<Event> findAdminEvents(
            @Param("status") EventStatus status,
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("city") String city,
            @Param("venueId") Long venueId,
            @Param("date") LocalDateTime date,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
    
    
    List<Event> findByStatusAndStartTimeBefore(EventStatus status, LocalDateTime time);

    List<Event> findByStatusAndEndTimeBefore(EventStatus status, LocalDateTime time);
}

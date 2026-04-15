package com.project.ems.event.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.auth.repository.UserRepository;
import com.project.ems.category.repository.CategoryRepository;
import com.project.ems.common.entity.Category;
import com.project.ems.common.entity.Event;
import com.project.ems.common.entity.Status;
import com.project.ems.common.entity.Ticket;
import com.project.ems.common.entity.User;
import com.project.ems.common.entity.Venue;
import com.project.ems.common.exception.CategoryNotFoundException;
import com.project.ems.common.exception.EventNotFoundException;
import com.project.ems.common.exception.OrganizerNotVerifiedException;
import com.project.ems.common.exception.TicketMismatchException;
import com.project.ems.common.exception.UnauthorizedException;
import com.project.ems.common.exception.UserNotFoundException;
import com.project.ems.common.exception.VenueNotFoundException;
import com.project.ems.event.dto.EventCreateRequest;
import com.project.ems.event.dto.EventDetailDTO;
import com.project.ems.event.dto.EventListDTO;
import com.project.ems.event.dto.EventStatusRequest;
import com.project.ems.event.dto.EventUpdateRequest;
import com.project.ems.event.mapper.EventMapper;
import com.project.ems.event.repository.EventRepository;
import com.project.ems.organizer.repository.OrganizerProfileRepository;
import com.project.ems.venue.repository.VenueRepository;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final EventMapper eventMapper;

    public EventService(EventRepository eventRepository,
                        CategoryRepository categoryRepository,
                        VenueRepository venueRepository,
                        UserRepository userRepository,
                        OrganizerProfileRepository organizerProfileRepository,
                        EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.venueRepository = venueRepository;
        this.userRepository = userRepository;
        this.organizerProfileRepository = organizerProfileRepository;
        this.eventMapper = eventMapper;
    }

    @Transactional(readOnly = true)
    public EventDetailDTO getEventById(Long id, Long userId, String role) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        if ("ADMIN".equals(role)) {
            return eventMapper.toDetailDTO(event);
        }

        if ("ORGANIZER".equals(role)) {
            if (!event.getOrganizer().getId().equals(userId)) {
                throw new UnauthorizedException("Access denied");
            }
            return eventMapper.toDetailDTO(event);
        }

        if (event.getStatus() != Event.EventStatus.PUBLISHED
                || event.getStartTime().isBefore(LocalDateTime.now())
                || event.getCategory().getStatus() != Status.ACTIVE
                || event.getVenue().getStatus() != Status.ACTIVE) {
            throw new EventNotFoundException(id);
        }

        return eventMapper.toDetailDTO(event);
    }

    @Transactional
    public EventDetailDTO createEvent(EventCreateRequest request, Long userId, String role) {
        Long effectiveOrganizerId;

        if ("ADMIN".equals(role)) {
            if (request.getOrganizerId() == null) {
                throw new IllegalArgumentException("Admin must specify organizerId when creating an event");
            }
            effectiveOrganizerId = request.getOrganizerId();
        } else {
            effectiveOrganizerId = userId;
        }

        boolean isVerified = organizerProfileRepository.existsByUserIdAndVerifiedTrue(effectiveOrganizerId);

        if (!isVerified) {
            throw new OrganizerNotVerifiedException("Organizer is not verified");
        }

        User organizer = userRepository.findById(effectiveOrganizerId)
                .orElseThrow(() -> new UserNotFoundException("Organizer not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new VenueNotFoundException("Venue not found"));

        int ticketSum = request.getTickets().stream().mapToInt(t -> t.getTotalQuantity()).sum();

        if (ticketSum != request.getTotalQuantity()) {
            throw new TicketMismatchException("Ticket quantity mismatch with event capacity");
        }

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setFullDescription(request.getFullDescription());
        event.setCategory(category);
        event.setVenue(venue);
        event.setOrganizer(organizer);
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setCancellationDeadline(request.getCancellationDeadline());
        event.setIsCancellable(request.getCancellationDeadline() != null);
        event.setStatus(Event.EventStatus.DRAFT);
        event.setCreatedBy(creator);
        event.setCreatedAt(LocalDateTime.now());

        List<Ticket> tickets = request.getTickets().stream().map(t -> {
            Ticket ticket = new Ticket();
            ticket.setName(t.getName());
            ticket.setPrice(t.getPrice());
            ticket.setTotalQuantity(t.getTotalQuantity());
            ticket.setAvailableQuantity(t.getTotalQuantity());
            ticket.setSaleStartTime(t.getSaleStartTime());
            ticket.setSaleEndTime(t.getSaleEndTime());
            ticket.setStatus(Status.ACTIVE);
            ticket.setEvent(event);
            return ticket;
        }).toList();

        event.setTickets(tickets);

        return eventMapper.toDetailDTO(eventRepository.save(event));
    }

    @Transactional
    public EventDetailDTO updateEvent(Long id, EventUpdateRequest request, Long userId, String role) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        if (!"ADMIN".equals(role) && !event.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedException("You can only edit your own events");
        }

        if (event.getStatus() != Event.EventStatus.DRAFT && event.getStatus() != Event.EventStatus.APPROVED) {
            throw new IllegalStateException("Event can only be updated in DRAFT or APPROVED state");
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            event.setTitle(request.getTitle().trim());
        }

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            event.setDescription(request.getDescription().trim());
        }

        if (request.getFullDescription() != null && !request.getFullDescription().isBlank()) {
            event.setFullDescription(request.getFullDescription().trim());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
            event.setCategory(category);
        }

        if (request.getVenueId() != null) {
            Venue venue = venueRepository.findById(request.getVenueId())
                    .orElseThrow(() -> new VenueNotFoundException("Venue not found"));
            event.setVenue(venue);
        }

        if (request.getStartTime() != null) event.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) event.setEndTime(request.getEndTime());
        if (request.getCancellationDeadline() != null) {
            event.setCancellationDeadline(request.getCancellationDeadline());
            event.setIsCancellable(true);
        }

        User updater = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        event.setUpdatedBy(updater);
        event.setUpdatedAt(LocalDateTime.now());

        return eventMapper.toDetailDTO(eventRepository.save(event));
    }

    @Transactional
    public void deleteEvent(Long id, Long userId, String role) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        if (!"ADMIN".equals(role) && !event.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own events");
        }

        if (event.getStatus() != Event.EventStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT events can be deleted");
        }

        eventRepository.delete(event);
    }

    @Transactional
    public EventDetailDTO changeStatus(Long id, EventStatusRequest request, Long userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String roleName = user.getRole().getName().name();
        String newStatus = request.getStatus().toUpperCase();

        switch (newStatus) {
            case "PENDING_APPROVAL":
                if (!roleName.equals("ORGANIZER") || !event.getOrganizer().getId().equals(userId))
                    throw new UnauthorizedException("Only organizer can submit for approval");
                if (event.getStatus() != Event.EventStatus.DRAFT)
                    throw new IllegalStateException("Only DRAFT events can be submitted for approval");
                event.setStatus(Event.EventStatus.PENDING_APPROVAL);
                break;

            case "APPROVED":
                if (!roleName.equals("ADMIN"))
                    throw new UnauthorizedException("Only admin can approve events");
                event.setStatus(Event.EventStatus.APPROVED);
                event.setApprovedBy(user);
                event.setApprovedAt(LocalDateTime.now());
                break;

            case "REJECTED":
                if (!roleName.equals("ADMIN"))
                    throw new UnauthorizedException("Only admin can reject events");
                event.setStatus(Event.EventStatus.REJECTED);
                break;

            case "PUBLISHED":
                if (roleName.equals("ADMIN") || (roleName.equals("ORGANIZER") && event.getOrganizer().getId().equals(userId))) {
                    if (event.getStatus() != Event.EventStatus.APPROVED)
                        throw new IllegalStateException("Event must be APPROVED before publishing");
                    event.setStatus(Event.EventStatus.PUBLISHED);
                    event.setPublishedAt(LocalDateTime.now());
                } else {
                    throw new UnauthorizedException("Only organizer or admin can publish events");
                }
                break;

            case "CANCELLED":
                boolean isAdmin = roleName.equals("ADMIN");
                boolean isOwner = event.getOrganizer().getId().equals(userId);

                if (!isAdmin && !isOwner) {
                    throw new UnauthorizedException("Only organizer or admin can cancel events");
                }

                if (!isAdmin && event.getStatus() == Event.EventStatus.PUBLISHED) {
                    throw new IllegalStateException("Organizer cannot cancel a published event");
                }

                event.setStatus(Event.EventStatus.CANCELLED);
                event.setCancelledBy(user);
                event.setCancelledAt(LocalDateTime.now());
                break;

            default:
                throw new IllegalArgumentException("Invalid status: " + request.getStatus());
        }

        return eventMapper.toDetailDTO(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public Page<EventListDTO> getEvents(
            String search,
            Long categoryId,
            String city,
            Long venueId,
            String date,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String status,
            Long userId,
            String role,
            int page,
            int size,
            String sort
    ) {
        String[] parts = sort.split(",");

        String property = parts[0];
        String direction = (parts.length > 1) ? parts[1] : "asc";

        Sort.Direction dir = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, property));

        String normalizedSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        String normalizedCity = (city != null && !city.isBlank()) ? city.trim() : null;

        LocalDateTime dateFilter = null;
        if (date != null && !date.isBlank()) {
            dateFilter = LocalDate.parse(date).atStartOfDay();
        }

        if ("ADMIN".equals(role)) {
            Event.EventStatus filterStatus = null;
            if (status != null && !status.isBlank()) {
                try {
                    filterStatus = Event.EventStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException ignored) {
                }
            }
            return eventRepository.findAdminEvents(
                    filterStatus, normalizedSearch, categoryId, normalizedCity,
                    venueId, dateFilter, minPrice, maxPrice, pageable
            ).map(eventMapper::toListDTO);
        }

        if ("ORGANIZER".equals(role)) {
            Event.EventStatus filterStatus = null;
            if (status != null && !status.isBlank()) {
                try {
                    filterStatus = Event.EventStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException ignored) {
                }
            }
            return eventRepository.findOrganizerEvents(
                    userId, filterStatus, normalizedSearch, categoryId, normalizedCity,
                    venueId, dateFilter, minPrice, maxPrice, pageable
            ).map(eventMapper::toListDTO);
        }

        return eventRepository.findPublicEvents(
                LocalDateTime.now(), normalizedSearch, categoryId, normalizedCity,
                venueId, dateFilter, minPrice, maxPrice, pageable
        ).map(eventMapper::toListDTO);
    }
}

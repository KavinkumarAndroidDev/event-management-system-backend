package com.project.ems.event.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.venue.repository.VenueRepository;
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
import com.project.ems.common.exception.UserNotFoundException;
import com.project.ems.common.exception.VenueNotFoundException;
import com.project.ems.event.dto.EventCreateRequest;
import com.project.ems.event.dto.EventDetailDTO;
import com.project.ems.event.dto.EventListDTO;
import com.project.ems.event.mapper.EventMapper;
import com.project.ems.event.repository.EventRepository;
import com.project.ems.organizer.repository.OrganizerProfileRepository;


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
    public List<EventListDTO> listActiveEvents() {
        return eventRepository
                .findByStatusAndStartTimeAfter(Event.EventStatus.PUBLISHED, LocalDateTime.now())
                .stream()
                .map(eventMapper::toListDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventDetailDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        return eventMapper.toDetailDTO(event);
    }
    
    @Transactional
    public Event createEvent(EventCreateRequest request, Long sessionUserId) {
        boolean isVerified = organizerProfileRepository
                .existsByUserIdAndVerifiedTrue(sessionUserId);

        if (!isVerified) {
            throw new OrganizerNotVerifiedException("Organizer is not verified");
        }

        User organizer = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new UserNotFoundException ("User not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new VenueNotFoundException("Venue not found"));

        int ticketSum = request.getTickets()
                .stream()
                .mapToInt(t -> t.getTotalQuantity())
                .sum();

        if (ticketSum != request.getTotalQuantity()) {
            throw new TicketMismatchException("Ticket quantity mismatch with event capacity");
        }

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

        event.setStatus(Event.EventStatus.PENDING_APPROVAL);
        event.setCreatedBy(organizer);
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

        return eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Page<EventListDTO> getEvents(
            String search,
            Long categoryId,
            String city,
            String fromDate,
            String toDate,
            int page,
            int size,
            String[] sort
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(getSortOrders(sort)));

        LocalDateTime from = fromDate != null ? LocalDateTime.parse(fromDate) : null;
        LocalDateTime to = toDate != null ? LocalDateTime.parse(toDate) : null;

        Specification<Event> spec = EventSpecification.filter(
                search, categoryId, city, from, to
        );

        return eventRepository.findAll(spec, pageable)
                .map(eventMapper::toListDTO);
    }
    
    
    private List<Sort.Order> getSortOrders(String[] sort) {
        List<Sort.Order> orders = new ArrayList<>();

        for (String s : sort) {
            String[] parts = s.split(",");
            orders.add(new Sort.Order(
                    parts[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                    parts[0]
            ));
        }

        return orders;
    }
}



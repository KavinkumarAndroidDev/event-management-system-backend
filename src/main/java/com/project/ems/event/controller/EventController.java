package com.project.ems.event.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.common.exception.UnauthorizedException;
import com.project.ems.event.dto.EventCreateRequest;
import com.project.ems.event.dto.EventDetailDTO;
import com.project.ems.event.service.EventService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

//    @GetMapping
//    public ResponseEntity<List<EventListDTO>> listActiveEvents() {
//        return ResponseEntity.ok(eventService.listActiveEvents());
//    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDetailDTO> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }
    
    @PostMapping
    public ResponseEntity<?> createEvent(@Valid @RequestBody EventCreateRequest eventCreateRequest, HttpSession session){
    	Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new UnauthorizedException("User not logged in");
        }
		return ResponseEntity.ok(eventService.createEvent(eventCreateRequest, userId));
    	
    }
    
    @GetMapping
    public ResponseEntity<?> getEvents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime,asc") String[] sort
    ) {
        return ResponseEntity.ok(
                eventService.getEvents(search, categoryId, city, fromDate, toDate, page, size, sort)
        );
    }
}
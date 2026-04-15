package com.project.ems.event.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.project.ems.common.entity.Event;
import com.project.ems.common.entity.Event.EventStatus;
import com.project.ems.event.repository.EventRepository;

import jakarta.transaction.Transactional;

@Service
public class EventStatusScheduler {

    private final EventRepository eventRepository;

    public EventStatusScheduler(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Scheduled(fixedRate = 60000) // every minute
    @Transactional
    public void updateEventStatuses() {

        LocalDateTime now = LocalDateTime.now();

        // 1. Move PUBLISHED -> ONGOING
        List<Event> toStart = eventRepository
            .findByStatusAndStartTimeBefore(EventStatus.PUBLISHED, now);

        for (Event event : toStart) {
            if (event.getEndTime().isAfter(now)) {
                event.setStatus(EventStatus.ONGOING);
            }
        }

        // 2. Move ONGOING -> COMPLETED
        List<Event> toComplete = eventRepository
            .findByStatusAndEndTimeBefore(EventStatus.ONGOING, now);

        for (Event event : toComplete) {
            event.setStatus(EventStatus.COMPLETED);
        }
    }
}
package com.project.ems.event.mapper;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.project.ems.common.entity.Event;
import com.project.ems.common.entity.Ticket;
import com.project.ems.event.dto.CategoryDTO;
import com.project.ems.event.dto.EventDetailDTO;
import com.project.ems.event.dto.EventListDTO;
import com.project.ems.event.dto.OrganizerDTO;
import com.project.ems.event.dto.VenueDTO;

@Component
public class EventMapper {

    public EventDetailDTO toDetailDTO(Event event) {
        if (event == null) return null;

        EventDetailDTO dto = new EventDetailDTO();

        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setFullDescription(event.getFullDescription());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setIsCancellable(event.getIsCancellable());
        dto.setCancellationDeadline(event.getCancellationDeadline());

        dto.setCategory(mapCategory(event));
        dto.setVenue(mapVenue(event));
        dto.setOrganizer(mapOrganizer(event));

        if (event.getVenue() != null) {
            dto.setCapacity(event.getVenue().getCapacity());
        }

        return dto;
    }

    public EventListDTO toListDTO(Event event) {
        if (event == null) return null;

        EventListDTO dto = new EventListDTO();

        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setStartTime(event.getStartTime());

        if (event.getVenue() != null) {
            dto.setVenueName(event.getVenue().getName());
            dto.setCity(event.getVenue().getCity());
        }

        if (event.getCategory() != null) {
            dto.setCategoryName(event.getCategory().getName());
        }
        dto.setStartingPrice(
        	    getStartingPrice(event) != null 
        	        ? getStartingPrice(event).doubleValue() 
        	        : null
        	);
        return dto;
    }

    private CategoryDTO mapCategory(Event event) {
        if (event.getCategory() == null) return null;

        CategoryDTO dto = new CategoryDTO();
        dto.setId(event.getCategory().getId());
        dto.setName(event.getCategory().getName());
        return dto;
    }

    private VenueDTO mapVenue(Event event) {
        if (event.getVenue() == null) return null;

        VenueDTO dto = new VenueDTO();
        dto.setId(event.getVenue().getId());
        dto.setName(event.getVenue().getName());
        dto.setAddress(event.getVenue().getAddress());
        dto.setCity(event.getVenue().getCity());
        dto.setState(event.getVenue().getState());
        return dto;
    }

    private OrganizerDTO mapOrganizer(Event event) {
        if (event.getOrganizer() == null) return null;

        OrganizerDTO dto = new OrganizerDTO();
        dto.setId(event.getOrganizer().getId());
        dto.setFullName(event.getOrganizer().getFullName());
        return dto;
    }
    
    private BigDecimal getStartingPrice(Event event) {
        if (event.getTickets() == null || event.getTickets().isEmpty()) {
            return null;
        }

        return event.getTickets()
                .stream()
                .map(Ticket::getPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(null);
    }
}
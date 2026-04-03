package com.project.ems.venue.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.common.entity.Status;
import com.project.ems.common.entity.Venue;
import com.project.ems.common.exception.VenueNotFoundException;
import com.project.ems.venue.dto.VenueCreateRequest;
import com.project.ems.venue.dto.VenueStatusRequest;
import com.project.ems.venue.dto.VenueUpdateRequest;
import com.project.ems.venue.repository.VenueRepository;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    // GET /venues — PUBLIC sees ACTIVE only; ADMIN sees all
    @Transactional(readOnly = true)
    public List<Venue> listVenues(String city, String role) {
        if ("ADMIN".equals(role)) {
            if (city != null && !city.isBlank()) {
                return venueRepository.findByCityIgnoreCase(city.trim());
            }
            return venueRepository.findAll();
        }
        if (city != null && !city.isBlank()) {
            return venueRepository.findByStatusAndCityIgnoreCase(Status.ACTIVE, city.trim());
        }
        return venueRepository.findByStatus(Status.ACTIVE);
    }

    // GET /venues — PUBLIC (kept for internal use)
    @Transactional(readOnly = true)
    public List<Venue> listActiveVenues(String city) {

        if (city != null && !city.isBlank()) {
            return venueRepository.findByStatusAndCityIgnoreCase(Status.ACTIVE, city.trim());
        }

        return venueRepository.findByStatus(Status.ACTIVE);
    }

    // GET /venues/{id} — PUBLIC sees ACTIVE only; ADMIN sees all statuses
    @Transactional(readOnly = true)
    public Venue getVenueById(Long id, String role) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new VenueNotFoundException("Venue not found"));
        if (!"ADMIN".equals(role) && venue.getStatus() != Status.ACTIVE) {
            throw new VenueNotFoundException("Venue not found");
        }
        return venue;
    }

    // GET /venues/{id} — PUBLIC (kept for internal use)
    @Transactional(readOnly = true)
    public Venue getVenueById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new VenueNotFoundException("Venue not found"));
    }

    // POST /venues — ADMIN
    @Transactional
    public Venue createVenue(VenueCreateRequest request) {

        Venue venue = new Venue();
        venue.setName(request.getName().trim());
        venue.setAddress(request.getAddress().trim());
        venue.setCity(request.getCity().trim());
        venue.setState(request.getState().trim());
        venue.setCapacity(request.getCapacity());
        venue.setStatus(Status.ACTIVE);
        venue.setCreatedAt(LocalDateTime.now());

        return venueRepository.save(venue);
    }

    // PUT /venues/{id} — ADMIN
    @Transactional
    public Venue updateVenue(Long id, VenueUpdateRequest request) {

        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new VenueNotFoundException("Venue not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            venue.setName(request.getName().trim());
        }

        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            venue.setAddress(request.getAddress().trim());
        }

        if (request.getCity() != null && !request.getCity().isBlank()) {
            venue.setCity(request.getCity().trim());
        }

        if (request.getState() != null && !request.getState().isBlank()) {
            venue.setState(request.getState().trim());
        }

        if (request.getCapacity() != null) {
            venue.setCapacity(request.getCapacity());
        }

        return venueRepository.save(venue);
    }

    // PATCH /venues/{id}/status — ADMIN
    @Transactional
    public Venue updateVenueStatus(Long id, VenueStatusRequest request) {

        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new VenueNotFoundException("Venue not found"));

        venue.setStatus(request.getStatus());
        return venueRepository.save(venue);
    }
}

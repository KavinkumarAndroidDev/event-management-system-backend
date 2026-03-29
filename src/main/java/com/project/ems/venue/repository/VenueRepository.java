package com.project.ems.venue.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.ems.common.entity.Status;
import com.project.ems.common.entity.Venue;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    List<Venue> findByStatus(Status status);

    List<Venue> findByStatusAndCityIgnoreCase(Status status, String city);
}

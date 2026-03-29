package com.project.ems.event.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.project.ems.common.entity.Event;
import com.project.ems.common.entity.Event.EventStatus;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event>{
	
	List<Event> findByStatus(EventStatus status);
	
	List<Event> findByStatusAndStartTimeAfter(EventStatus status, LocalDateTime time);
}
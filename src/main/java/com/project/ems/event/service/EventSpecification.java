package com.project.ems.event.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.project.ems.common.entity.Event;

import jakarta.persistence.criteria.Predicate;

public class EventSpecification {

	public static Specification<Event> filter(String search, Long categoryId, String city, LocalDateTime fromDate,
			LocalDateTime toDate) {
		return (root, query, cb) -> {

			List<Predicate> predicates = new ArrayList<>();

			// Only published events
			predicates.add(cb.equal(root.get("status"), Event.EventStatus.PUBLISHED));

			// Search (title + description)
			if (search != null && !search.isEmpty()) {
				String like = "%" + search.toLowerCase() + "%";
				predicates.add(cb.or(cb.like(cb.lower(root.get("title")), like),
						cb.like(cb.lower(root.get("description")), like)));
			}

			// Category filter
			if (categoryId != null) {
				predicates.add(cb.equal(root.get("category").get("id"), categoryId));
			}

			// City filter (JOIN venue)
			if (city != null) {
				predicates.add(cb.equal(cb.lower(root.get("venue").get("city")), city.toLowerCase()));
			}

			// Date filter
			if (fromDate != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), fromDate));
			}

			if (toDate != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("startTime"), toDate));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
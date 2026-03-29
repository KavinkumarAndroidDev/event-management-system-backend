package com.project.ems.common.exception;

public class EventNotFoundException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EventNotFoundException(Long id) {
        super("Event not found with id: " + id);
    }
}
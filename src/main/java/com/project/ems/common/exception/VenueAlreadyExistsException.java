package com.project.ems.common.exception;

public class VenueAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public VenueAlreadyExistsException(String message) {
        super(message);
    }
}

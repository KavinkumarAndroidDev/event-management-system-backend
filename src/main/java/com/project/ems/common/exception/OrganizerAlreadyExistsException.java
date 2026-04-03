package com.project.ems.common.exception;

public class OrganizerAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public OrganizerAlreadyExistsException(String message) {
        super(message);
    }
}

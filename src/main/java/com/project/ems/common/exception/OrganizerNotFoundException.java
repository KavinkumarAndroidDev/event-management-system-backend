package com.project.ems.common.exception;

public class OrganizerNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public OrganizerNotFoundException(String message) {
        super(message);
    }
}

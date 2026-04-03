package com.project.ems.common.exception;

public class RegistrationNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RegistrationNotFoundException(Long id) {
        super("Registration not found with id: " + id);
    }
}

package com.project.ems.common.exception;

public class ParticipantNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ParticipantNotFoundException(Long id) {
        super("Participant not found with id: " + id);
    }
}

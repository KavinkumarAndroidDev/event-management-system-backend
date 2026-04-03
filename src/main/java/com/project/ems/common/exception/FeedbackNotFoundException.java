package com.project.ems.common.exception;

public class FeedbackNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public FeedbackNotFoundException(Long id) {
        super("Feedback not found with id: " + id);
    }
}

package com.project.ems.common.exception;

public class DuplicateFeedbackException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DuplicateFeedbackException(String msg) {
        super(msg);
    }
}

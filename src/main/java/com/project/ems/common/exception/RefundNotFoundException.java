package com.project.ems.common.exception;

public class RefundNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RefundNotFoundException(Long id) {
        super("Refund not found with id: " + id);
    }
}

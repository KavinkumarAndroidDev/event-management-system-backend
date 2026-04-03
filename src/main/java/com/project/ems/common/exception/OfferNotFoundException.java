package com.project.ems.common.exception;

public class OfferNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public OfferNotFoundException(Long id) {
        super("Offer not found with id: " + id);
    }

    public OfferNotFoundException(String msg) {
        super(msg);
    }
}

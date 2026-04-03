package com.project.ems.common.exception;

public class OfferAlreadyExistsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public OfferAlreadyExistsException(String msg) {
        super(msg);
    }
}

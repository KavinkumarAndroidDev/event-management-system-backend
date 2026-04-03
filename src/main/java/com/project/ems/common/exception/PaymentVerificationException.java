package com.project.ems.common.exception;

public class PaymentVerificationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PaymentVerificationException(String msg) {
        super(msg);
    }
}

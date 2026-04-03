package com.project.ems.common.exception;

public class RazorpayOrderException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RazorpayOrderException(String msg) {
        super(msg);
    }
}

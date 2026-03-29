package com.project.ems.common.exception;

public class OtpAlreadySentException  extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public OtpAlreadySentException(String message) {
        super(message);
    }
}

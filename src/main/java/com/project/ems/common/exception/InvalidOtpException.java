package com.project.ems.common.exception;

public class InvalidOtpException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidOtpException(String message) {
        super(message);
    }
}

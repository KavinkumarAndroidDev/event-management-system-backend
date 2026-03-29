package com.project.ems.common.exception;

public class InvalidCredentialsException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidCredentialsException(String msg) {
        super(msg);
    }
}
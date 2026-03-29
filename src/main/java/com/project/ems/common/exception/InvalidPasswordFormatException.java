package com.project.ems.common.exception;

public class InvalidPasswordFormatException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public InvalidPasswordFormatException(String message) {
        super(message);
    }
    public InvalidPasswordFormatException(String message, Throwable cause) {
        super(message, cause);
    }

}

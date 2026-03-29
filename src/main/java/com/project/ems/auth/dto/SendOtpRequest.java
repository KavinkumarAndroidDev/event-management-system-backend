package com.project.ems.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class SendOtpRequest {
	@NotBlank(message = "Enter the email or phone number")
    private String identifier;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
    
    
}

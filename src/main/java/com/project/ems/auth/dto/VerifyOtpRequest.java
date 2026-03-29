package com.project.ems.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VerifyOtpRequest {
	@NotBlank(message = "Enter email or phone number")
    private String identifier;
	
	@NotBlank(message = "Enter the otp!")
	@Pattern(regexp = "^[0-9]{6}$", message = "Otp must be 6 digits")
    private String otp;
    
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getOtp() {
		return otp;
	}
	public void setOtp(String otp) {
		this.otp = otp;
	}
    
    
}
package com.project.ems.auth.dto;

import com.project.ems.common.entity.User;

import jakarta.validation.constraints.*;

public class RegisterRequest {

	@NotBlank
	@Email(message = "Invalid email format")
	private String email;

	@NotBlank
	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,}$", message = "Password must be strong")
	private String password;

	@NotBlank
	@Size(min = 3, message = "Name must be at least 3 characters")
	private String fullName;

	@NotBlank
	@Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
	private String phone;

	private User.Gender gender;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public User.Gender getGender() {
		return gender;
	}

	public void setGender(User.Gender gender) {
		this.gender = gender;
	}

	

}

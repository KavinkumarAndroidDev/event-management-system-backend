package com.project.ems.auth.dto;

public class AuthResponse {

	private Long id;
	private String email;
	private String fullName;
	private String role;
	private String accessToken;
	private String refreshToken;

	public AuthResponse(Long id, String email, String fullName, String role, String accessToken, String refreshToken) {
		this.id = id;
		this.email = email;
		this.fullName = fullName;
		this.role = role;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getFullName() {
		return fullName;
	}

	public String getRole() {
		return role;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}
}

package com.project.ems.participant.dto;

import com.project.ems.common.entity.User.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ParticipantCreateRequest {

    @NotNull(message = "Registration item ID is required")
    private Long registrationItemId;

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "Phone is required")
    @Size(max = 20)
    private String phone;

    private Gender gender;

    public Long getRegistrationItemId() {
        return registrationItemId;
    }

    public void setRegistrationItemId(Long registrationItemId) {
        this.registrationItemId = registrationItemId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }
}

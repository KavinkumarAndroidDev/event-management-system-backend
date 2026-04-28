package com.project.ems.contact.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.contact.dto.ContactRequest;
import com.project.ems.contact.service.ContactService;

import jakarta.validation.Valid;

@RestController
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping("/contact")
    public ResponseEntity<String> sendContactMessage(@Valid @RequestBody ContactRequest request) {
        contactService.sendContactMail(request);
        return ResponseEntity.ok("Message sent successfully");
    }
}

package com.project.ems.contact.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.project.ems.contact.dto.ContactRequest;

import jakarta.mail.internet.MimeMessage;

@Service
public class ContactService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${contact.mail.to:${spring.mail.username:}}")
    private String contactEmail;

    public ContactService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendContactMail(ContactRequest request) {
        if (contactEmail == null || contactEmail.isBlank()) {
            throw new IllegalStateException("Contact email is not configured");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(contactEmail);
            helper.setReplyTo(request.getEmail());
            helper.setSubject("SyncEvent Contact: " + request.getSubject());
            helper.setText(buildBody(request), false);
            mailSender.send(message);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to send contact email", ex);
        }
    }

    private String buildBody(ContactRequest request) {
        return "Name: " + request.getName() + "\n"
                + "Email: " + request.getEmail() + "\n"
                + "Phone: " + (request.getPhone() == null || request.getPhone().isBlank() ? "-" : request.getPhone()) + "\n\n"
                + request.getMessage();
    }
}

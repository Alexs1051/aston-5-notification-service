package org.aston.learning.stage2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmailRequest {

    @NotBlank(message = "Email recipient is mandatory")
    @Email(message = "Email should be valid")
    private String toEmail;

    @NotBlank(message = "Subject is mandatory")
    @Size(min = 1, max = 200, message = "Subject must be between 1 and 200 characters")
    private String subject;

    @NotBlank(message = "Message is mandatory")
    @Size(min = 1, max = 2000, message = "Message must be between 1 and 2000 characters")
    private String message;

    // Constructors
    public EmailRequest() {}

    public EmailRequest(String toEmail, String subject, String message) {
        this.toEmail = toEmail;
        this.subject = subject;
        this.message = message;
    }

    // Getters and setters
    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "EmailRequest{" +
                "toEmail='" + toEmail + '\'' +
                ", subject='" + subject + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
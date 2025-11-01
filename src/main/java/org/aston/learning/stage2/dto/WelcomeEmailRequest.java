package org.aston.learning.stage2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class WelcomeEmailRequest {

    @NotBlank(message = "Email recipient is mandatory")
    @Email(message = "Email should be valid")
    private String toEmail;

    @NotBlank(message = "User name is mandatory")
    private String userName;

    // Constructors
    public WelcomeEmailRequest() {}

    public WelcomeEmailRequest(String toEmail, String userName) {
        this.toEmail = toEmail;
        this.userName = userName;
    }

    // Getters and setters
    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "WelcomeEmailRequest{" +
                "toEmail='" + toEmail + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
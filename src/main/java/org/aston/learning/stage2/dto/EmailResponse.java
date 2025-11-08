package org.aston.learning.stage2.dto;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "notifications")
public class EmailResponse extends RepresentationModel<EmailResponse> {
    private boolean success;
    private String message;
    private String email;

    public EmailResponse() {}

    public EmailResponse(boolean success, String message, String email) {
        this.success = success;
        this.message = message;
        this.email = email;
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "EmailResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
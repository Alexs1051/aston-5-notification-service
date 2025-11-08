package org.aston.learning.stage2.dto;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "notificationTypes")
public class NotificationTypeResponse extends RepresentationModel<NotificationTypeResponse> {
    private String type;
    private String description;
    private String endpoint;

    public NotificationTypeResponse() {}

    public NotificationTypeResponse(String type, String description, String endpoint) {
        this.type = type;
        this.description = description;
        this.endpoint = endpoint;
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
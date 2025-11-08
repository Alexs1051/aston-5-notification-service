package org.aston.learning.stage2.dto;

import org.springframework.hateoas.RepresentationModel;

public class HealthResponse extends RepresentationModel<HealthResponse> {
    private String status;
    private String service;
    private long timestamp;

    public HealthResponse() {}

    public HealthResponse(String status, String service, long timestamp) {
        this.status = status;
        this.service = service;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "HealthResponse{" +
                "status='" + status + '\'' +
                ", service='" + service + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
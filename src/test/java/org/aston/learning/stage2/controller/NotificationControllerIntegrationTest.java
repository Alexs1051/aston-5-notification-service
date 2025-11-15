package org.aston.learning.stage2.controller;

import org.aston.learning.stage2.dto.*;
import org.aston.learning.stage2.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(NotificationController.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.config.import=optional:file:.env[.properties]",
        "eureka.client.enabled=false"
})
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmailService emailService;

    @Test
    void sendCustomEmail_ShouldReturnSuccess() throws Exception {
        // Arrange
        EmailRequest request = new EmailRequest("test@example.com", "Test Subject", "Test Message");

        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void sendWelcomeEmail_ShouldReturnSuccess() throws Exception {
        // Arrange
        WelcomeEmailRequest request = new WelcomeEmailRequest("test@example.com", "Test User");

        doNothing().when(emailService).sendUserCreatedEmail(anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/notifications/welcome")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void sendCustomEmail_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // Arrange
        EmailRequest request = new EmailRequest("invalid-email", "Test Subject", "Test Message");

        // Act & Assert
        mockMvc.perform(post("/api/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Email should be valid")));
    }

    @Test
    void sendCustomEmail_WithBlankEmail_ShouldReturnBadRequest() throws Exception {
        // Arrange
        EmailRequest request = new EmailRequest("", "Test Subject", "Test Message");

        // Act & Assert
        mockMvc.perform(post("/api/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Email recipient is mandatory")));
    }

    @Test
    void healthCheck_ShouldReturnServiceStatus() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/notifications/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("OK"))
                .andExpect(jsonPath("$.status").value("Notification Service is running"));
    }
}
package org.aston.learning.stage2.controller;

import org.aston.learning.stage2.dto.*;
import org.aston.learning.stage2.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationController notificationController;

    private EmailRequest validEmailRequest;
    private WelcomeEmailRequest validWelcomeRequest;

    @BeforeEach
    void setUp() {
        validEmailRequest = new EmailRequest();
        validEmailRequest.setToEmail("test@example.com");
        validEmailRequest.setSubject("Test Subject");
        validEmailRequest.setMessage("Test Message");

        validWelcomeRequest = new WelcomeEmailRequest();
        validWelcomeRequest.setToEmail("test@example.com");
        validWelcomeRequest.setUserName("Test User");
    }

    @Test
    void sendCustomEmail_WithValidRequest_ShouldReturnSuccessResponse() {
        // Arrange
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act
        ResponseEntity<EmailResponse> response = notificationController.sendCustomEmail(validEmailRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertTrue(response.getBody().getMessage().contains("Email sent successfully"));

        verify(emailService, times(1))
                .sendCustomEmail("test@example.com", "Test Subject", "Test Message");
    }

    @Test
    void sendCustomEmail_WhenEmailServiceThrowsException_ShouldReturnErrorResponse() {
        // Arrange
        String errorMessage = "SMTP server unavailable";
        doThrow(new RuntimeException(errorMessage))
                .when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act
        ResponseEntity<EmailResponse> response = notificationController.sendCustomEmail(validEmailRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertTrue(response.getBody().getMessage().contains("Failed to send email"));
        assertTrue(response.getBody().getMessage().contains(errorMessage));

        verify(emailService, times(1))
                .sendCustomEmail("test@example.com", "Test Subject", "Test Message");
    }

    @Test
    void sendWelcomeEmail_WithValidRequest_ShouldReturnSuccessResponse() {
        // Arrange
        doNothing().when(emailService).sendUserCreatedEmail(anyString(), anyString());

        // Act
        ResponseEntity<EmailResponse> response = notificationController.sendWelcomeEmail(validWelcomeRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertTrue(response.getBody().getMessage().contains("Welcome email sent successfully"));

        verify(emailService, times(1))
                .sendUserCreatedEmail("test@example.com", "Test User");
    }

    @Test
    void sendWelcomeEmail_WhenEmailServiceThrowsException_ShouldReturnErrorResponse() {
        // Arrange
        String errorMessage = "Mail server connection failed";
        doThrow(new RuntimeException(errorMessage))
                .when(emailService).sendUserCreatedEmail(anyString(), anyString());

        // Act
        ResponseEntity<EmailResponse> response = notificationController.sendWelcomeEmail(validWelcomeRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertTrue(response.getBody().getMessage().contains("Failed to send welcome email"));
        assertTrue(response.getBody().getMessage().contains(errorMessage));

        verify(emailService, times(1))
                .sendUserCreatedEmail("test@example.com", "Test User");
    }

    @Test
    void healthCheck_ShouldReturnOkStatusWithCurrentTimestamp() {
        // Arrange
        long beforeTest = System.currentTimeMillis();

        // Act
        ResponseEntity<HealthResponse> response = notificationController.healthCheck();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Notification Service is running", response.getBody().getStatus());
        assertEquals("OK", response.getBody().getService());
        assertTrue(response.getBody().getTimestamp() >= beforeTest);
    }

    @Test
    void sendCustomEmail_ShouldCallEmailServiceWithCorrectParameters() {
        // Arrange
        EmailRequest specificRequest = new EmailRequest(
                "specific@example.com",
                "Specific Subject",
                "Specific Message"
        );
        doNothing().when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act
        notificationController.sendCustomEmail(specificRequest);

        // Assert
        verify(emailService, times(1))
                .sendCustomEmail("specific@example.com", "Specific Subject", "Specific Message");
    }

    @Test
    void sendWelcomeEmail_ShouldCallEmailServiceWithCorrectParameters() {
        // Arrange
        WelcomeEmailRequest specificRequest = new WelcomeEmailRequest("user@example.com", "John Doe");
        doNothing().when(emailService).sendUserCreatedEmail(anyString(), anyString());

        // Act
        notificationController.sendWelcomeEmail(specificRequest);

        // Assert
        verify(emailService, times(1))
                .sendUserCreatedEmail("user@example.com", "John Doe");
    }

    @Test
    void sendCustomEmail_WhenServiceFails_ShouldPreserveOriginalEmailInResponse() {
        // Arrange
        String recipientEmail = "preserved@example.com";
        EmailRequest request = new EmailRequest(recipientEmail, "Subject", "Message");

        doThrow(new RuntimeException("Service failure"))
                .when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act
        ResponseEntity<EmailResponse> response = notificationController.sendCustomEmail(request);

        // Assert
        assertNotNull(response.getBody());
        assertEquals(recipientEmail, response.getBody().getEmail());
        // Check that the email is saved in the email field, not necessarily in the message
    }

    @Test
    void sendWelcomeEmail_WhenServiceFails_ShouldPreserveOriginalEmailInResponse() {
        // Arrange
        String recipientEmail = "preserved@example.com";
        WelcomeEmailRequest request = new WelcomeEmailRequest(recipientEmail, "User Name");

        doThrow(new RuntimeException("Service failure"))
                .when(emailService).sendUserCreatedEmail(anyString(), anyString());

        // Act
        ResponseEntity<EmailResponse> response = notificationController.sendWelcomeEmail(request);

        // Assert
        assertNotNull(response.getBody());
        assertEquals(recipientEmail, response.getBody().getEmail());
        // Check that the email is saved in the email field, not necessarily in the message
    }

    @Test
    void sendCustomEmail_WhenServiceFails_ShouldReturnCorrectErrorMessage() {
        // Arrange
        String recipientEmail = "test@example.com";
        EmailRequest request = new EmailRequest(recipientEmail, "Subject", "Message");
        String errorMessage = "Specific error occurred";

        doThrow(new RuntimeException(errorMessage))
                .when(emailService).sendCustomEmail(anyString(), anyString(), anyString());

        // Act
        ResponseEntity<EmailResponse> response = notificationController.sendCustomEmail(request);

        // Assert
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to send email: " + errorMessage, response.getBody().getMessage());
        assertEquals(recipientEmail, response.getBody().getEmail());
    }

    @Test
    void sendWelcomeEmail_WhenServiceFails_ShouldReturnCorrectErrorMessage() {
        // Arrange
        String recipientEmail = "test@example.com";
        WelcomeEmailRequest request = new WelcomeEmailRequest(recipientEmail, "User Name");
        String errorMessage = "Specific welcome error";

        doThrow(new RuntimeException(errorMessage))
                .when(emailService).sendUserCreatedEmail(anyString(), anyString());

        // Act
        ResponseEntity<EmailResponse> response = notificationController.sendWelcomeEmail(request);

        // Assert
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to send welcome email: " + errorMessage, response.getBody().getMessage());
        assertEquals(recipientEmail, response.getBody().getEmail());
    }
}
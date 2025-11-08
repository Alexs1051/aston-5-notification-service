package org.aston.learning.stage2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.aston.learning.stage2.dto.*;
import org.aston.learning.stage2.service.EmailService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/notifications")
@Validated
@Tag(name = "Notification Management", description = "APIs for sending notifications and checking service health")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private EmailService emailService;

    @PostMapping("/email")
    @Operation(summary = "Send custom email", description = "Send a custom email to the specified recipient")
    public ResponseEntity<EmailResponse> sendCustomEmail(
            @Valid @RequestBody EmailRequest emailRequest) {

        logger.info("Sending custom email to: {} with subject: {}",
                emailRequest.getToEmail(), emailRequest.getSubject());

        try {
            emailService.sendCustomEmail(
                    emailRequest.getToEmail(),
                    emailRequest.getSubject(),
                    emailRequest.getMessage()
            );

            logger.info("Successfully sent email to: {}", emailRequest.getToEmail());

            EmailResponse response = new EmailResponse(
                    true,
                    "Email sent successfully to: " + emailRequest.getToEmail(),
                    emailRequest.getToEmail()
            );

            // HATEOAS links
            addCommonLinks(response);
            response.add(linkTo(methodOn(NotificationController.class).sendWelcomeEmail(null)).withRel("send-welcome-email"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to send email to: {}", emailRequest.getToEmail(), e);

            EmailResponse response = new EmailResponse(
                    false,
                    "Failed to send email: " + e.getMessage(),
                    emailRequest.getToEmail()
            );

            // HATEOAS links
            addCommonLinks(response);

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/welcome")
    @Operation(summary = "Send welcome email", description = "Send a welcome email to a new user")
    public ResponseEntity<EmailResponse> sendWelcomeEmail(
            @Valid @RequestBody WelcomeEmailRequest welcomeRequest) {

        logger.info("Sending welcome email to: {}", welcomeRequest.getToEmail());

        try {
            emailService.sendUserCreatedEmail(
                    welcomeRequest.getToEmail(),
                    welcomeRequest.getUserName()
            );

            logger.info("Successfully sent welcome email to: {}", welcomeRequest.getToEmail());

            EmailResponse response = new EmailResponse(
                    true,
                    "Welcome email sent successfully to: " + welcomeRequest.getToEmail(),
                    welcomeRequest.getToEmail()
            );

            // HATEOAS links
            addCommonLinks(response);
            response.add(linkTo(methodOn(NotificationController.class).sendCustomEmail(null)).withRel("send-custom-email"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", welcomeRequest.getToEmail(), e);

            EmailResponse response = new EmailResponse(
                    false,
                    "Failed to send welcome email: " + e.getMessage(),
                    welcomeRequest.getToEmail()
            );

            // HATEOAS links
            addCommonLinks(response);

            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check the health status of the notification service")
    public ResponseEntity<HealthResponse> healthCheck() {
        HealthResponse response = new HealthResponse(
                "Notification Service is running",
                "OK",
                System.currentTimeMillis()
        );

        // HATEOAS links
        response.add(linkTo(methodOn(NotificationController.class).healthCheck()).withSelfRel());
        response.add(linkTo(methodOn(NotificationController.class).sendCustomEmail(null)).withRel("send-custom-email"));
        response.add(linkTo(methodOn(NotificationController.class).sendWelcomeEmail(null)).withRel("send-welcome-email"));

        return ResponseEntity.ok(response);
    }

    private void addCommonLinks(EmailResponse response) {
        response.add(linkTo(methodOn(NotificationController.class).healthCheck()).withRel("health-check"));
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<EmailResponse> handleValidationExceptions(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        EmailResponse response = new EmailResponse(false, errorMessage, null);
        return ResponseEntity.badRequest().body(response);
    }
}
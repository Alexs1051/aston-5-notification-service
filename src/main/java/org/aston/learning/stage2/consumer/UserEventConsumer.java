package org.aston.learning.stage2.consumer;

import org.aston.learning.stage2.event.UserEvent;
import org.aston.learning.stage2.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UserEventConsumer.class);

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "user-events", groupId = "notification-group")
    public void consumeUserEvent(UserEvent event) {
        logger.info("Received user event: {} for user: {}", event.getEventType(), event.getEmail());

        try {
            switch (event.getEventType()) {
                case "USER_CREATED":
                    handleUserCreated(event);
                    break;

                case "USER_DELETED":
                    handleUserDeleted(event);
                    break;

                default:
                    logger.warn("Unknown event type: {} for user: {}", event.getEventType(), event.getEmail());
                    handleUnknownEvent(event);
            }
        } catch (Exception e) {
            logger.error("Failed to process user event for email: {}", event.getEmail(), e);
        }
    }

    private void handleUserCreated(UserEvent event) {
        try {
            emailService.sendUserCreatedEmail(event.getEmail(), event.getUserName());
            logger.info("Successfully sent creation email to: {}", event.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send creation email to: {}", event.getEmail(), e);
            throw e; // Throw exception for retry, for Kafka
        }
    }

    private void handleUserDeleted(UserEvent event) {
        try {
            emailService.sendUserDeletedEmail(event.getEmail(), event.getUserName());
            logger.info("Successfully sent deletion email to: {}", event.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send deletion email to: {}", event.getEmail(), e);
            throw e; // Throw exception for retry, for Kafka
        }
    }

    private void handleUnknownEvent(UserEvent event) {
        logger.warn("No action taken for unknown event type: {}", event.getEventType());
    }
}
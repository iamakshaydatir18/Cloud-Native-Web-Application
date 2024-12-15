package com.example.demo.service;

import org.springframework.stereotype.Service;
import com.example.demo.RequestResponseObjects.UserPayload;
import com.example.demo.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final String TOPIC_ARN = System.getenv("TOPIC_ARN");
    private final ObjectMapper objectMapper;

    public NotificationService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public String PublishMessage(User createdUser) {
        try {
            logger.info("Publishing json object to Topic SNS " + TOPIC_ARN);
            SnsClient snsClient = SnsClient.builder().build();
            UserPayload userPayload = new UserPayload(createdUser.getEmail(),createdUser.getFirst_name(), createdUser.getId(),
                    createdUser.getAccount_created());
            logger.info(
                    "Publishing json object to Topic SNS " + TOPIC_ARN + " Payload is -- " + userPayload.toString());
            // Publish the JSON message to SNS
            String response = publishMessageToSns(snsClient, userPayload);
            // Close the SNS client
            snsClient.close();
            return response;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String publishMessageToSns(SnsClient snsClient, UserPayload userPayload) {
        try {
            // Convert the Java object to a JSON string
            String jsonMessage = objectMapper.writeValueAsString(userPayload);

            logger.info("Json String " + jsonMessage);
            // Create a PublishRequest
            PublishRequest publishRequest = PublishRequest.builder().message(jsonMessage) // The JSON payload
                    .topicArn(TOPIC_ARN) // SNS topic ARN
                    .build();

            // Publish the message
            PublishResponse publishResponse = snsClient.publish(publishRequest);
            System.out.println("Message sent. Message ID: " + publishResponse.messageId());
            logger.info("Message sent. Message ID: " + publishResponse.messageId());
            return "Message sent. Message ID: " + publishResponse.messageId();
        } catch (Exception e) {
            System.err.println("Error publishing message to SNS: " + e.getMessage());
            logger.error("Error publishing message to SNS: " + e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
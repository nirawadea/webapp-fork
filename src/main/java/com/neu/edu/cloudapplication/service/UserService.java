package com.neu.edu.cloudapplication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.edu.cloudapplication.model.User;
import com.neu.edu.cloudapplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final SnsClient snsClient;

    @Value("${sns.topic.arn}")
    private String snsTopicArn;

    public UserService() {
        this.snsClient = SnsClient.builder().build(); // Initialize SNS client
    }

    public User createUser(User user) {
        String encryptedPwd = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPwd);
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser.isPresent()) {
            throw new RuntimeException("User already exists:" + existingUser.get().getEmail());
        }
        // Save the new user to the database
        User savedUser = userRepository.save(user);
        // Publish user creation message to SNS
        publishUserCreatedMessage(savedUser);
        return savedUser;
    }

    private void publishUserCreatedMessage(User user) {
        try {
            // Prepare the message payload
            Map<String, String> messagePayload = new HashMap<>();
            messagePayload.put("email", user.getEmail());
            messagePayload.put("userId", user.getId().toString());

            // Convert the payload to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String messageJson = objectMapper.writeValueAsString(messagePayload);

            // Create and send PublishRequest
            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(snsTopicArn)
                    .message(messageJson)
                    .build();

            PublishResponse publishResponse = snsClient.publish(publishRequest);

            // Log the message ID returned by SNS (optional)
            System.out.println("Message sent to SNS with ID: " + publishResponse.messageId());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to publish user creation message to SNS", e);
        }
    }

    public User updateUser(User user, String email) {
        // Find the existing user by email
        Optional<User> existingUserOpt = userRepository.findByEmail(email);
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                String encryptedPwd = passwordEncoder.encode(user.getPassword());
                existingUser.setPassword(encryptedPwd);
            }
            return userRepository.save(existingUser);

        } else {
            throw new RuntimeException("User doesn't exist with email: " + email);
        }
    }

    public User getUser(String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        } else {
            throw new RuntimeException("User doesn't exist with email: " + email);
        }
    }

    public void updatePicData(Date uploadDate, String fileName, String url, String email) {
        userRepository.updatePicFields(uploadDate, fileName, url, email);

    }
}

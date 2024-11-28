package com.neu.edu.cloudapplication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.Map;

public class SecretsManagerService {

    private final SecretsManagerClient secretsManagerClient;

    public SecretsManagerService() {
        this.secretsManagerClient = SecretsManagerClient.builder()
                .region(Region.of(System.getenv("aws.s3.region")))
                .build();
    }

    public Map<String, String> getSecret(String secretName) {
        GetSecretValueRequest secretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse secretValueResponse = secretsManagerClient.getSecretValue(secretValueRequest);

        // Parse secret value as JSON
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(secretValueResponse.secretString(), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse secret value", e);
        }
    }
}

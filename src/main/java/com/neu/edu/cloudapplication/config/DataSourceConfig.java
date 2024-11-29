package com.neu.edu.cloudapplication.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.edu.cloudapplication.controllers.HealthController;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Value("${aws.secret.name}")
    private String secretName;

    @Value("${aws.s3.region}")
    private String regionName;

    private final static Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    public DataSource dataSource() {
        Map<String, String> secrets = getSecret(secretName);
        String dbUrl = secrets.get("DB_URL");
        String username = secrets.get("DB_USERNAME");
        String password = secrets.get("DB_PASSWORD");
        return DataSourceBuilder.create()
                .url(dbUrl)
                .username(username)
                .password(password)
                .type(HikariDataSource.class)
                .build();
    }

    public Map<String, String> getSecret(String secretName) {
        SecretsManagerClient secretsManagerClient = SecretsManagerClient.builder().region(Region.of(regionName)).build();
        GetSecretValueRequest secretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build();

        GetSecretValueResponse secretValueResponse;

        // Parse secret value as JSON format
        try {
            secretValueResponse = secretsManagerClient.getSecretValue(secretValueRequest);
            ObjectMapper objectMapper = new ObjectMapper();
            logger.info("SecretString: " + secretValueResponse.secretString());
            return objectMapper.readValue(secretValueResponse.secretString(), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse secret value", e);
        }
    }
}

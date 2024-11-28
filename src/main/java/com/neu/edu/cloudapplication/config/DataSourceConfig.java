package com.neu.edu.cloudapplication.config;

import com.neu.edu.cloudapplication.service.SecretsManagerService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.util.Map;

public class DataSourceConfig {

    @Value("${aws.secret.name}")
    private String secretName;

    private final SecretsManagerService secretsManagerService;

    public DataSourceConfig() {
        this.secretsManagerService = new SecretsManagerService();
    }

    @Bean
    public DataSource dataSource() {
        Map<String, String> secrets = secretsManagerService.getSecret(secretName);

        String dbUrl = secrets.get("DB_URL");
        String username = secrets.get("DB_USERNAME");
        String password = secrets.get("DB_PASSWORD");

        // Configure HikariCP DataSource
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Optional: Customize HikariCP settings
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setIdleTimeout(600000);
        config.setConnectionTimeout(30000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }


}

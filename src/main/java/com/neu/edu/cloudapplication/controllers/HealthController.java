package com.neu.edu.cloudapplication.controllers;

import com.neu.edu.cloudapplication.dao.DbConnection;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RestController
public class HealthController {

    @Autowired
    private DbConnection dbConnection;

    @Autowired
    private StatsDClient statsDClient;


    private final static Logger logger = LoggerFactory.getLogger(HealthController.class);
    
    @GetMapping("/healthz")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> health(@RequestBody(required = false) String requestPayload) {
        // Start timer to measure endpoint response time
        long startTime = System.currentTimeMillis();

        // Track endpoint call count
        statsDClient.incrementCounter("endpoint.healthz.get");

        if (requestPayload != null && !requestPayload.isEmpty()) {
            statsDClient.incrementCounter("endpoint.healthz.bad_request.count");
            statsDClient.recordExecutionTime("endpoint.healthz.response.time", System.currentTimeMillis() - startTime);
            logger.info("endpoint.healthz.get bad request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        }
        DataSource dataSource = dbConnection.createConnection();
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null) {
                // Track successful DB connection for health check
                statsDClient.incrementCounter("endpoint.healthz.success.count");

                // Record response time metric
                statsDClient.recordExecutionTime("endpoint.healthz.response.time", System.currentTimeMillis() - startTime);
                logger.info("endpoint.healthz.get hit successfully");
                return ResponseEntity.ok().header("Cache-Control", "no-cache").build();
            }
        } catch (SQLException e) {
            // Track DB connection failure
            statsDClient.incrementCounter("endpoint.healthz.db_failure.count");
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        // Track overall health check failure and response time
        statsDClient.incrementCounter("endpoint.healthz.failure.count");
        statsDClient.recordExecutionTime("endpoint.healthz.response.time", System.currentTimeMillis() - startTime);
        logger.info("endpoint.healthz.get SERVICE_UNAVAILABLE");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}

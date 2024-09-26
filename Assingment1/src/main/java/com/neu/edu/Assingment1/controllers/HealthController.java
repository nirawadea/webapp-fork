package com.neu.edu.Assingment1.controllers;

import com.neu.edu.Assingment1.dao.DbConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/healthz")
    public ResponseEntity<?> health(@RequestBody(required = false) String requestPayload) {
        if (requestPayload != null && !requestPayload.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        DataSource dataSource = dbConnection.createConnection();
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null) {
                return ResponseEntity.ok().header("Cache-Control", "no-cache").build();
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}

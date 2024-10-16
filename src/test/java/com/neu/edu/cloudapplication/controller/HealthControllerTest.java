package com.neu.edu.cloudapplication.controller;

import com.neu.edu.cloudapplication.controllers.HealthController;
import com.neu.edu.cloudapplication.dao.DbConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class HealthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DbConnection dbConnection;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @BeforeEach
    public void setup() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(dbConnection.createConnection()).thenReturn(dataSource);
    }

    @Test
    public void testHealthCheckSuccess() throws Exception {
        // Simulate successful DB connection
        when(dataSource.getConnection()).thenReturn(connection);

        mockMvc.perform(get("/healthz")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache"));
    }

    @Test
    public void testHealthCheckBadRequestWithBody() throws Exception {
        // Simulate invalid request with body
        mockMvc.perform(get("/healthz")
                        .content("some-payload")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testHealthCheckServiceUnavailable() throws Exception {
        // Simulate DB connection failure
        when(dataSource.getConnection()).thenThrow(new SQLException());

        mockMvc.perform(get("/healthz")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable());
    }
}

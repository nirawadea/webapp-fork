package com.neu.edu.cloudapplication.controller;

import com.neu.edu.cloudapplication.controllers.UserController;
import com.neu.edu.cloudapplication.model.User;
import com.neu.edu.cloudapplication.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("John");
        user.setLastName("Doe");
    }

    @Test
    public void createUser_ShouldReturnOk_WhenUserCreatedSuccessfully() {
        // Mock the behavior of createUser to return the user object
        when(userService.createUser(any(User.class))).thenReturn(user);

        ResponseEntity<String> response = userController.createUser(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User created successfully", response.getBody());
    }


    @Test
    public void createUser_ShouldReturnBadRequest_WhenExceptionThrown() {
        doThrow(new RuntimeException("User already exists")).when(userService).createUser(any(User.class));

        ResponseEntity<String> response = userController.createUser(user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User already exists", response.getBody());
    }

    @Test
    public void updateUser_ShouldReturnOk_WhenUserUpdatedSuccessfully() {
        when(userService.updateUser(any(User.class), anyString())).thenReturn(user);

        ResponseEntity<String> response = userController.updateUser("test@example.com", user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User updated successfully", response.getBody());
    }

    @Test
    public void updateUser_ShouldReturnBadRequest_WhenEmailMismatch() {
        user.setEmail("mismatch@example.com");

        ResponseEntity<String> response = userController.updateUser("test@example.com", user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email cannot be modified!", response.getBody());
    }

    @Test
    public void updateUser_ShouldReturnBadRequest_WhenExceptionThrown() {
        doThrow(new RuntimeException("User doesn't exist")).when(userService).updateUser(any(User.class), anyString());

        ResponseEntity<String> response = userController.updateUser("test@example.com", user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User doesn't exist", response.getBody());
    }

    @Test
    public void getUser_ShouldReturnOk_WhenUserFound() {
        when(userService.getUser(anyString())).thenReturn(user);

        ResponseEntity<?> response = userController.getUser("test@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    public void getUser_ShouldReturnBadRequest_WhenExceptionThrown() {
        when(userService.getUser(anyString())).thenThrow(new RuntimeException("User doesn't exist"));

        ResponseEntity<?> response = userController.getUser("test@example.com");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User doesn't exist", response.getBody());
    }
}

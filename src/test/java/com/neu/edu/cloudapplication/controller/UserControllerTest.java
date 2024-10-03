package com.neu.edu.cloudapplication.controller;

import com.neu.edu.cloudapplication.controllers.UserController;
import com.neu.edu.cloudapplication.model.User;
import com.neu.edu.cloudapplication.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("John");
        user.setLastName("Doe");

        // Mock the current HTTP request for query parameter checking
        when(httpServletRequest.getRequestURI()).thenReturn("/v1/user/createUser");
        when(httpServletRequest.getQueryString()).thenReturn(null);
        ServletRequestAttributes attributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(attributes);

        // Mock the SecurityContextHolder to provide the authenticated email
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void createUser_ShouldReturnCreated_WhenUserCreatedSuccessfully() {
        // Mock the behavior of createUser to return the user object
        when(userService.createUser(any(User.class))).thenReturn(user);

        ResponseEntity<String> response = userController.createUser(user);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User created successfully", response.getBody());
    }

    @Test
    public void createUser_ShouldReturnBadRequest_WhenExceptionThrown() {
        // Mock the exception thrown by the service
        doThrow(new RuntimeException("User already exists")).when(userService).createUser(any(User.class));

        ResponseEntity<String> response = userController.createUser(user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User already exists", response.getBody());
    }

    @Test
    public void updateUser_ShouldReturnNoContent_WhenUserUpdatedSuccessfully() {
        // Mock the authenticated email
        when(authentication.getName()).thenReturn("test@example.com");

        // Mock successful update without any response body (204 No Content)
        when(userService.updateUser(any(User.class), anyString())).thenReturn(user);

        ResponseEntity<?> response = userController.updateUser(user);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void updateUser_ShouldReturnBadRequest_WhenEmailMismatch() {
        // Mock the authenticated email
        when(authentication.getName()).thenReturn("test@example.com");

        // Set a different email in the user object
        user.setEmail("mismatch@example.com");

        ResponseEntity<?> response = userController.updateUser(user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email cannot be modified!", response.getBody());
    }

    @Test
    public void updateUser_ShouldReturnBadRequest_WhenExceptionThrown() {
        // Mock the authenticated email
        when(authentication.getName()).thenReturn("test@example.com");

        // Mock exception during update
        doThrow(new RuntimeException("User doesn't exist")).when(userService).updateUser(any(User.class), anyString());

        ResponseEntity<?> response = userController.updateUser(user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User doesn't exist", response.getBody());
    }

    @Test
    public void getUser_ShouldReturnOk_WhenUserFound() {
        // Mock the authenticated email
        when(authentication.getName()).thenReturn("test@example.com");

        when(userService.getUser(anyString())).thenReturn(user);

        ResponseEntity<?> response = userController.getUser();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    public void getUser_ShouldReturnBadRequest_WhenExceptionThrown() {
        // Mock the authenticated email
        when(authentication.getName()).thenReturn("test@example.com");

        when(userService.getUser(anyString())).thenThrow(new RuntimeException("User doesn't exist"));

        ResponseEntity<?> response = userController.getUser();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User doesn't exist", response.getBody());
    }
}

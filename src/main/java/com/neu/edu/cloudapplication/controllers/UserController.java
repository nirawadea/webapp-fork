package com.neu.edu.cloudapplication.controllers;

import com.neu.edu.cloudapplication.model.User;
import com.neu.edu.cloudapplication.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/v1/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        if (hasQueryParameters()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Query parameters not allowed.");
        }
        try {
            userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    @PutMapping("/self")
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        if (hasQueryParameters()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Query parameters not allowed.");
        }

        try {
            // Get the authenticated user's email from the security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedEmail = authentication.getName();  // This is the email used in basic auth

            // Ensure that the user is trying to update their own email (email cannot be modified)
            if (!authenticatedEmail.equals(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email cannot be modified!");
            }

            userService.updateUser(user, authenticatedEmail);
            return ResponseEntity.noContent().build();  // Return 204 No Content on success
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @GetMapping("/self")
    public ResponseEntity<?> getUser() {
        if (hasQueryParameters()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Query parameters not allowed.");
        }

        try {
            // Get the authenticated user's email from the security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedEmail = authentication.getName();  // The email used in basic authentication

            // Fetch the user details from the database using the authenticated email
            User user = userService.getUser(authenticatedEmail);
            return ResponseEntity.ok().body(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    private boolean hasQueryParameters() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        return queryString != null || requestURI.contains("?");
    }
}

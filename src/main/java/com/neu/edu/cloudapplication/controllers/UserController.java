package com.neu.edu.cloudapplication.controllers;

import com.neu.edu.cloudapplication.model.User;
import com.neu.edu.cloudapplication.service.S3Service;
import com.neu.edu.cloudapplication.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private S3Service s3Service;

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        if (hasQueryParameters()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Query parameters not allowed.");
        }
        if (!isValidEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This Email is not valid!!!");
        }
        try {
            userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    private boolean isValidEmail(String email) {

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(emailRegex);
    }

    @PutMapping("/self")
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        if (hasQueryParameters()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Query parameters not allowed.");
        }

        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedEmail = authentication.getName();


            if (!authenticatedEmail.equals(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email cannot be modified!");
            }

            userService.updateUser(user, authenticatedEmail);
            return ResponseEntity.noContent().build();
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

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedEmail = authentication.getName();  // The email used in basic authentication


            User user = userService.getUser(authenticatedEmail);
            return ResponseEntity.ok().body(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/self/pic")
    public ResponseEntity<?> uploadPic(@RequestParam(value="profilePic") MultipartFile profilePic) {
        System.out.println("inside upload pic");
        if (hasQueryParameters()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Query parameters not allowed.");
        }
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedEmail = authentication.getName();  // The email used in basic authentication
            User user = userService.getUser(authenticatedEmail);
            System.out.println("File Content Type: " + profilePic.getContentType());
            if (!profilePic.getContentType().startsWith("image/")) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            System.out.println(profilePic.getOriginalFilename());
            String bucket_name = s3Service.uploadFile(user.getEmail()+ "/" + profilePic.getOriginalFilename(), profilePic);
            System.out.println("bucket_name "+bucket_name);
            String url = bucket_name + "/" + user.getEmail() + "/" + profilePic.getOriginalFilename();
            System.out.println("url "+url);
            user.setFile_name(profilePic.getOriginalFilename());
            user.setS3_bucket_path(url);
            userService.updateUser(user, authenticatedEmail);
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

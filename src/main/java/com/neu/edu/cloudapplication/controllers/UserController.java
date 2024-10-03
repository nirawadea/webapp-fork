package com.neu.edu.cloudapplication.controllers;

import com.neu.edu.cloudapplication.model.User;
import com.neu.edu.cloudapplication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/createUser")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        try {
            userService.createUser(user);
            return ResponseEntity.ok().body("User created successfully");
        }
        catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }
    @PutMapping("/updateUser/{email}")
    public ResponseEntity<String> updateUser(@PathVariable String email, @RequestBody User user) {
        try {
            if (!email.equals(user.getEmail())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email cannot be modified!");
            }
            System.out.println("Updating user details for: " + email);
            userService.updateUser(user, email);
            return ResponseEntity.ok().body("User updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @GetMapping("/getUser/{email}")
    public ResponseEntity<?> getUser(@PathVariable String email){
        try {
            User user = userService.getUser(email);
            return ResponseEntity.ok().body(user);
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }
}

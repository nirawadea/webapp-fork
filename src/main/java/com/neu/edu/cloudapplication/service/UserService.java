package com.neu.edu.cloudapplication.service;

import com.neu.edu.cloudapplication.model.User;
import com.neu.edu.cloudapplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    public User createUser(User user) {
        String encryptedPwd = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPwd);
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser.isPresent()) {
            throw new RuntimeException("User already exists:" + existingUser.get().getEmail());
        }
        return userRepository.save(user);
    }

    public User updateUser(User user, String email) {
        // Find the existing user by email
        Optional<User> existingUserOpt = userRepository.findByEmail(email);
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                String encryptedPwd = passwordEncoder.encode(user.getPassword());
                existingUser.setPassword(encryptedPwd);
            }
            return userRepository.save(existingUser);

        } else {
            throw new RuntimeException("User doesn't exist with email: " + email);
        }
    }

    public User getUser(String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        } else {
            throw new RuntimeException("User doesn't exist with email: " + email);
        }
    }

    public void updatePicData(Date uploadDate, String fileName, String url, String email) {
        userRepository.updatePicFields(uploadDate, fileName, url, email);

    }
}

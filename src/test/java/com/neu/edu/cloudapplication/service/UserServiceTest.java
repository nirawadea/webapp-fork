package com.neu.edu.cloudapplication.service;

import com.neu.edu.cloudapplication.model.User;
import com.neu.edu.cloudapplication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("test");
        user.setFirstName("John");
        user.setLastName("Doe");
    }

    @Test
    public void createUserTest() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encryptedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User createdUser = userService.createUser(user);

        assertNotNull(createdUser);
        assertEquals("encryptedPassword", createdUser.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void createUser_ShouldThrowException_WhenUserAlreadyExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(user));

        assertEquals("User already exists:test@example.com", exception.getMessage());
        verify(userRepository, never()).save(user);
    }

    @Test
    public void updateUser_ShouldUpdateUser_WhenUserExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encryptedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        user.setPassword("newPassword");
        user.setFirstName("UpdatedFirstName");

        User updatedUser = userService.updateUser(user, "test@example.com");

        assertNotNull(updatedUser);
        assertEquals("UpdatedFirstName", updatedUser.getFirstName());
        assertEquals("encryptedPassword", updatedUser.getPassword());
        verify(userRepository, times(1)).save(updatedUser);
    }

    @Test
    public void updateUser_ShouldThrowException_WhenUserDoesNotExist() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.updateUser(user, "test@example.com"));

        assertEquals("User doesn't exist with email: test@example.com", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void getUser_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        User foundUser = userService.getUser("test@example.com");

        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    public void getUser_ShouldThrowException_WhenUserDoesNotExist() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getUser("test@example.com"));

        assertEquals("User doesn't exist with email: test@example.com", exception.getMessage());
    }
}

package com.neu.edu.cloudapplication.service;

import com.neu.edu.cloudapplication.model.User;
import com.neu.edu.cloudapplication.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SnsClient snsClient;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeAll
    public static void setupEnvironment() {
        System.setProperty("aws.region", "us-east-1"); // Set the AWS region for the test environment
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set a mock SNS Topic ARN
        ReflectionTestUtils.setField(userService, "snsTopicArn", "arn:aws:sns:us-east-1:123456789012:myTopic");

        // Mock PublishResponse
        PublishResponse publishResponse = mock(PublishResponse.class);
        when(publishResponse.messageId()).thenReturn("mockMessageId");

        // Mock the snsClient's publish method to return the mock PublishResponse
        when(snsClient.publish(any(PublishRequest.class))).thenReturn(publishResponse);

        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("test");
        user.setFirstName("John");
        user.setLastName("Doe");
    }

//    @Test
//    public void createUserTest() {
//        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
//        when(passwordEncoder.encode(anyString())).thenReturn("encryptedPassword");
//        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
//            User savedUser = invocation.getArgument(0);
//            savedUser.setId("mockedId"); // Set a mock ID here to avoid NullPointerException
//            return savedUser;
//        });
//
//        User createdUser = userService.createUser(user);
//
//        assertNotNull(createdUser);
//        assertEquals("encryptedPassword", createdUser.getPassword());
//        verify(userRepository, times(1)).save(user);
//
//        // Verify that SNS publish was called
//        verify(snsClient, times(1)).publish(any(PublishRequest.class));
//    }

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

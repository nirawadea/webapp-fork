package com.neu.edu.cloudapplication.controllers;

import com.neu.edu.cloudapplication.model.ImageResponse;
import com.neu.edu.cloudapplication.model.User;
import com.neu.edu.cloudapplication.service.S3Service;
import com.neu.edu.cloudapplication.service.UserService;
import com.timgroup.statsd.StatsDClient;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import java.util.Date;

@RestController
@RequestMapping("/v1/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private StatsDClient statsDClient; // Injecting StatsDClient for metrics tracking

    private final static Logger logger = LoggerFactory.getLogger(HealthController.class);

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        statsDClient.incrementCounter("endpoint.createUser.post"); // Increment call count
        long startTime = System.currentTimeMillis();

        if (hasQueryParameters()) {
            logger.info("endpoint.createUser.post BAD_REQUEST Query parameters not allowed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Query parameters not allowed.");
        }
        if (!isValidEmail(user.getEmail())) {
            logger.info("endpoint.createUser.post BAD_REQUEST Email not valid");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This Email is not valid!!!");
        }
        try {
            userService.createUser(user);
            logger.info("endpoint.createUser.post User created Successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
        } catch (RuntimeException e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }finally {
            statsDClient.recordExecutionTime("endpoint.createUser.time", System.currentTimeMillis() - startTime); // Record execution time
        }

    }

    private boolean isValidEmail(String email) {

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(emailRegex);
    }

    @PutMapping("/self")
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        statsDClient.incrementCounter("endpoint.updateUser.put");
        long startTime = System.currentTimeMillis();

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
        }finally {
            statsDClient.recordExecutionTime("endpoint.updateUser.time", System.currentTimeMillis() - startTime);
        }
    }


    @GetMapping("/self")
    public ResponseEntity<?> getUser() {
        statsDClient.incrementCounter("endpoint.getUser.get");
        long startTime = System.currentTimeMillis();


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
        }finally {
            statsDClient.recordExecutionTime("endpoint.getUser.time", System.currentTimeMillis() - startTime);
        }
    }

    @PostMapping("/self/pic")
    public ResponseEntity<?> uploadPic(@RequestParam(value="profilePic") MultipartFile profilePic) {
        statsDClient.incrementCounter("endpoint.uploadPic.post");
        long startTime = System.currentTimeMillis();

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
            String bucket_name = s3Service.uploadFile(user.getId()+ "/" + profilePic.getOriginalFilename(), profilePic);
            System.out.println("bucket_name "+bucket_name);
            String url = bucket_name + "/" + user.getId() + "/" + profilePic.getOriginalFilename();
            System.out.println("url "+url);
            user.setUploadDate(new Date());
            user.setFile_name(profilePic.getOriginalFilename());
            user.setUrl(url);
            userService.updatePicData(new Date(), profilePic.getOriginalFilename(), url, user.getEmail());

            ImageResponse imageResponse = new ImageResponse(
                    user.getFile_name(),
                    user.getId(),
                    user.getUrl(),
                    user.getUploadDate(),
                    user.getId()
            );
            return ResponseEntity.ok().body(imageResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }finally {
            statsDClient.recordExecutionTime("endpoint.uploadPic.time", System.currentTimeMillis() - startTime);
        }
    }

    @GetMapping("/self/pic")
    public ResponseEntity<?> getPic() {
        statsDClient.incrementCounter("endpoint.getPic.get");
        long startTime = System.currentTimeMillis();
        System.out.println("inside get pic");
        if (hasQueryParameters()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Query parameters not allowed.");
        }
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedEmail = authentication.getName();  // The email used in basic authentication
            User user = userService.getUser(authenticatedEmail);
            if (user.getFile_name() != null) {
                ImageResponse imageResponse = new ImageResponse(
                        user.getFile_name(),
                        user.getId(),
                        user.getUrl(),
                        user.getUploadDate(),
                        user.getId()
                );
                return ResponseEntity.ok().body(imageResponse);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }finally {
            statsDClient.recordExecutionTime("endpoint.getPic.time", System.currentTimeMillis() - startTime);
        }
    }

    @DeleteMapping("/self/pic")
    public ResponseEntity<?> deletePic() {
        statsDClient.incrementCounter("endpoint.deletePic.delete");
        long startTime = System.currentTimeMillis();

        System.out.println("inside delete pic");
        if (hasQueryParameters()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Query parameters not allowed.");
        }
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedEmail = authentication.getName();
            User user = userService.getUser(authenticatedEmail);
            System.out.println("Deleting file from S3");
            String message = s3Service.deleteFileFromS3Bucket(user.getUrl(), user.getId().toString());
            System.out.println("message "+message);
            user.setUploadDate(null);
            user.setFile_name(null);
            user.setUrl(null);
            userService.updateUser(user, authenticatedEmail);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }finally {
            statsDClient.recordExecutionTime("endpoint.deletePic.time", System.currentTimeMillis() - startTime);
        }
    }


    private boolean hasQueryParameters() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        return queryString != null || requestURI.contains("?");
    }
}

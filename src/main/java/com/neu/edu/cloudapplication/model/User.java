package com.neu.edu.cloudapplication.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;


@Entity
@Data
public class User {
    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)", unique = true, nullable = false)
    private String id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password", length = 60)
    private String password;

    @Column(name = "account_created", updatable = false)
    private LocalDateTime accountCreated;

    @Column(name = "account_updated")
    private LocalDateTime accountUpdated;

    @Column(name = "file_name")
    private String file_name;

    private String url;

    @Column(name = "upload_date")
    private Date uploadDate;

    @Column(name = "token", unique = true)
    private String token;

    @Column(name = "is_verified")
    private boolean isVerified;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();  // Automatically generate a UUID as a String
        }
        accountCreated = LocalDateTime.now();
        accountUpdated = LocalDateTime.now();
        isVerified = false;
    }

    @PreUpdate
    protected void onUpdate() {
        accountUpdated = LocalDateTime.now();
    }


}

package com.neu.edu.cloudapplication.repository;

import com.neu.edu.cloudapplication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail (String email);
}

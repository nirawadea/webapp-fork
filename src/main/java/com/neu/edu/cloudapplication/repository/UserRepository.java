package com.neu.edu.cloudapplication.repository;

import com.neu.edu.cloudapplication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail (String email);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.uploadDate = :uploadDate, u.file_name = :fileName, u.url = :url WHERE u.email = :email")
    void updatePicFields(@Param("uploadDate") Date uploadDate,
                         @Param("fileName") String fileName,
                         @Param("url") String url,
                         @Param("email") String email);
}

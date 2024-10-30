package com.neu.edu.cloudapplication.controllers;

import com.neu.edu.cloudapplication.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/user")
public class S3Controller {

    @Autowired
    private S3Service s3Service;

    @PostMapping("/self/pic")
    @PreAuthorize("permitAll()")
    public String upload(@RequestParam("file") MultipartFile file){
        return s3Service.saveFile(file);
    }

    @DeleteMapping("/self/pic")
    @PreAuthorize("permitAll()")
    public  String deleteFile(@PathVariable("filename") String filename){
        return s3Service.deleteFile(filename);
    }
}

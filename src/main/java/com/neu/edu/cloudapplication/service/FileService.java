package com.neu.edu.cloudapplication.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String saveFile(MultipartFile file);
    String deleteFile(String filename);
}

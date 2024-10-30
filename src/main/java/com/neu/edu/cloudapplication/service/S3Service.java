package com.neu.edu.cloudapplication.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class S3Service implements FileService {

    private final S3Client s3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3Service(S3Client s3) {
        this.s3 = s3;
    }

    @Override
    public String saveFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        int count = 0;
        int maxTries = 3;
        while (true) {
            try {
                File fileToUpload = convertMultiPartToFile(file);
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(originalFilename)
                        .build();

                PutObjectResponse putObjectResponse = s3.putObject(putObjectRequest, RequestBody.fromFile(fileToUpload));

                // Clean up the local file after upload
                Files.deleteIfExists(fileToUpload.toPath());

                return putObjectResponse.eTag();  // eTag is the equivalent of content MD5
            } catch (IOException e) {
                if (++count == maxTries) throw new RuntimeException("File upload failed after " + maxTries + " attempts", e);
            }
        }
    }

    @Override
    public String deleteFile(String filename) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .build();

        s3.deleteObject(deleteObjectRequest);
        return "File deleted";
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}

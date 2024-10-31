package com.neu.edu.cloudapplication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;


@Service
public class S3Service {

    private final S3Client s3;
    private final Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3Service(S3Client s3) {
        this.s3 = s3;
    }

    public String uploadFile(String keyName, MultipartFile file) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            PutObjectResponse response = s3.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            return response.eTag(); // ETag is a unique identifier for the uploaded file
        } catch (IOException ioe) {
            logger.error("IOException: " + ioe.getMessage());
            return "File not uploaded: " + keyName;
        } catch (S3Exception s3Exception) {
            logger.error("S3Exception: " + s3Exception.awsErrorDetails().errorMessage());
            throw s3Exception;
        }
    }

    public String deleteFileFromS3Bucket(String fileUrl, int userId) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        String objectKey = userId + "/" + fileName;

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            DeleteObjectResponse deleteResponse = s3.deleteObject(deleteObjectRequest);

            logger.info("Deleted file: " + objectKey + " from bucket: " + bucketName);
            return "Successfully Deleted";
        } catch (S3Exception e) {
            logger.error("S3Exception: Failed to delete file from S3 - " + e.awsErrorDetails().errorMessage());
            throw e;
        }
    }
}

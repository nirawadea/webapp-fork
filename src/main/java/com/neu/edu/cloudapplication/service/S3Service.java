package com.neu.edu.cloudapplication.service;

import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private StatsDClient statsDClient;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3Service(S3Client s3) {
        this.s3 = s3;
    }

    public String uploadFile(String keyName, MultipartFile file) {
        long startTime = System.currentTimeMillis(); // Start time for upload operation
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            PutObjectResponse response = s3.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            statsDClient.recordExecutionTime("s3.upload.time", System.currentTimeMillis() - startTime); // Record execution time
            statsDClient.incrementCounter("s3.upload.success"); // Track successful upload
            logger.info("Pic Uploaded successfully");
            return bucketName; // ETag is a unique identifier for the uploaded file
        } catch (IOException ioe) {
            statsDClient.incrementCounter("s3.upload.failure"); // Track failed upload
            logger.error("IOException: " + ioe.getMessage());
            return "File not uploaded: " + keyName;
        } catch (S3Exception s3Exception) {
            statsDClient.incrementCounter("s3.upload.failure"); // Track failed upload due to S3 exception
            logger.error("S3Exception: " + s3Exception.awsErrorDetails().errorMessage());
            throw s3Exception;
        }
    }

    public String deleteFileFromS3Bucket(String fileUrl, String id) {
        long startTime = System.currentTimeMillis(); // Start time for delete operation
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        String objectKey = id + "/" + fileName;

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            DeleteObjectResponse deleteResponse = s3.deleteObject(deleteObjectRequest);

            statsDClient.recordExecutionTime("s3.delete.time", System.currentTimeMillis() - startTime); // Record execution time
            statsDClient.incrementCounter("s3.delete.success"); // Track successful deletion
            logger.info("Deleted file: " + objectKey + " from bucket: " + bucketName);
            return "Successfully Deleted";
        } catch (S3Exception e) {
            statsDClient.incrementCounter("s3.delete.failure"); // Track failed deletion
            logger.error("S3Exception: Failed to delete file from S3 - " + e.awsErrorDetails().errorMessage());
            throw e;
        } finally {
            statsDClient.recordExecutionTime("s3.delete.totalTime", System.currentTimeMillis() - startTime); // Record total time regardless of success or failure
        }
    }
}

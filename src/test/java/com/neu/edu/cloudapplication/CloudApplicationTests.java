package com.neu.edu.cloudapplication;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"aws.s3.region=us-east-1",
		"aws.s3.bucket=test-bucket",
		"sns.topic.arn=arn:aws:sns:us-east-1:557690612200:my-user-creation-topic",
		"aws.region=us-east-1"
})
class CloudApplicationTests {

	// Mock the S3 client so it doesn't require actual AWS connections
	@MockBean
	private S3Client s3Client;

	// Mock SnsClient for testing
	@MockBean
	private SnsClient snsClient;

	@Test
	void contextLoads() {
	}

}

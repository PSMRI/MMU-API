package com.iemr.mmu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/***
 * @purpose S3 client beans used to store diagnostic documents pushed from a van. Built
 *          once and shared, rather than re-built per request.
 */
@Configuration
public class S3ClientConfig {

	@Value("${aws.s3.access-key}")
	private String accessKey;

	@Value("${aws.s3.secret-key}")
	private String secretKey;

	@Value("${aws.s3.region}")
	private String region;

	private StaticCredentialsProvider credentialsProvider() {
		return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
	}

	@Bean
	public S3Client diagnosticDocumentS3Client() {
		return S3Client.builder().region(Region.of(region)).credentialsProvider(credentialsProvider()).build();
	}

	@Bean
	public S3Presigner diagnosticDocumentS3Presigner() {
		return S3Presigner.builder().region(Region.of(region)).credentialsProvider(credentialsProvider()).build();
	}
}

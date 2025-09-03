package com.demo.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URL;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${aws.s3.bucket:}")
    private String bucket;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (bucket == null || bucket.isBlank()) {
            return ResponseEntity.status(501).body(Map.of("error", "S3 not configured"));
        }
        String key = "uploads/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        S3Client s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
        s3.putObject(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes()));
        String url = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
        return ResponseEntity.ok(Map.of("url", url));
    }
}

package com.diploma.verivicationdipllom.controller;

import com.diploma.verivicationdipllom.domain.dto.UserDocumentDto;
import com.diploma.verivicationdipllom.domain.enums.BucketType;
import com.diploma.verivicationdipllom.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/images")
public class TestImageController {
    private final MinioService minioService;

    @Autowired
    public TestImageController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String bucketName = BucketType.VERIFICATION_USER.getValue();
            String objectName = file.getOriginalFilename();

            minioService.uploadImage(bucketName, objectName, file);

            return ResponseEntity.ok("Image uploaded successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading image: " + e.getMessage());
        }
    }

    @GetMapping("/download/{objectName}")
    public ResponseEntity<byte[]> downloadImage(@PathVariable String objectName) {
        try (InputStream inputStream = minioService.getImage("images", objectName)) {
            byte[] content = inputStream.readAllBytes();
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + objectName + "\"")
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }


    @PostMapping("/uploadUserDocument")
    public ResponseEntity<String> uploadUserDocument(@RequestParam("file") MultipartFile file) {
        try {
            var userDocumentDto = new UserDocumentDto();
            userDocumentDto.setUserDocument(file);
            userDocumentDto.setBucketType(BucketType.VERIFICATION_USER);
            userDocumentDto.setDocumentId(UUID.randomUUID());

            minioService.uploadDocument(userDocumentDto);

            return ResponseEntity.ok("Image uploaded successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading image: " + e.getMessage());
        }
    }

}

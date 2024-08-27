package com.diploma.verivicationdipllom.controller;

import com.diploma.verivicationdipllom.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

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
            String bucketName = "images";
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
}

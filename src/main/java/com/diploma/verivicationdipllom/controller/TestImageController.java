package com.diploma.verivicationdipllom.controller;

import com.diploma.verivicationdipllom.controller.rest_template.FaceMatchingServiceClient;
import com.diploma.verivicationdipllom.domain.dto.UserDocumentDto;
import com.diploma.verivicationdipllom.domain.enums.BucketType;
import com.diploma.verivicationdipllom.domain.repository.UserRepresentRepository;
import com.diploma.verivicationdipllom.service.FaceMatchingService;
import com.diploma.verivicationdipllom.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TestImageController {
    private final FaceMatchingService faceMatchingService;
    private final MinioService minioService;
    private final FaceMatchingServiceClient faceMatchingServiceTemp;
    private final UserRepresentRepository userRepresentRepository;

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

//    @GetMapping("/download/{objectName}")
//    public ResponseEntity<byte[]> downloadImage(@PathVariable String objectName) {
//        try (InputStream inputStream = minioService.getImage("images", objectName)) {
//            byte[] content = inputStream.readAllBytes();
//            return ResponseEntity.ok()
//                    .contentType(MediaType.IMAGE_JPEG)
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + objectName + "\"")
//                    .body(content);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(null);
//        }
//    }


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


    //------------------------------------------------------------------------------------------//

    @PostMapping("/faceMatching")
    public ResponseEntity<String> faceMatching(@RequestParam("document") MultipartFile document, @RequestParam("liveness") MultipartFile liveness) {
        faceMatchingService.faceMatching(document, liveness);
        return ResponseEntity.ok("Face matching successfully.");
    }



    @PostMapping("/test")
    public ResponseEntity<String> test(@RequestPart("document") MultipartFile document, @RequestPart("liveness") MultipartFile document1) throws IOException {
        faceMatchingServiceTemp.verifyTwoPics(document, document1);
        return ResponseEntity.ok("Face matching successfully.");
    }
    @GetMapping
    public String clearDatabase() {
        userRepresentRepository.deleteAll();
        return "Successfully cleared database.";
    }
}

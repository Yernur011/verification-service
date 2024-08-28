package com.diploma.verivicationdipllom.service;

import com.diploma.verivicationdipllom.controller.feign.FaceMatchingController;
import com.diploma.verivicationdipllom.domain.dto.UserDocumentDto;
import com.diploma.verivicationdipllom.domain.enums.BucketType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FaceMatchingService {
    final MinioService minioService;
    final FaceMatchingController faceMatchingController;

    @SneakyThrows
    ResponseEntity<?> checkUserByUserDocumentInDatabase(MultipartFile file) {
        var imageList = minioService.findAllFilesInUserDocumentBucket();

        for (var document : imageList) {
            var responseEntity = faceMatchingController.userDocumentCheck(file.getBytes(), document);
            if (responseEntity.getStatusCode().value() != HttpStatus.OK.value())
                return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("User with this face already exists");
        }

        var userDocument = new UserDocumentDto();
        userDocument.setDocumentId(UUID.randomUUID());
        userDocument.setBucketType(BucketType.VERIFICATION_USER);
        userDocument.setUserDocument(file);
        minioService.uploadDocument(userDocument);

        return ResponseEntity.ok("User with this face does not exist");
    }

    @SneakyThrows
    ResponseEntity<?> faceMatching(MultipartFile userDocument, MultipartFile userLive) {
       return faceMatchingController.getResultByFaceMatching(userDocument.getBytes(), userLive.getBytes());
    }

}

package com.diploma.verivicationdipllom.controller.feign;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Component
@RequestMapping("")
public interface FaceMatchingController {
    @PostMapping("")
    ResponseEntity<?> getResultByFaceMatching(byte[] userDoc, byte[] userLive);

    @PostMapping("")
    ResponseEntity<?> userDocumentCheck(byte[] userDocRequest, byte[] userDocFromDataBase);
}

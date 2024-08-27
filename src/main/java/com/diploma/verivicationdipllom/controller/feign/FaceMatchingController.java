package com.diploma.verivicationdipllom.controller.feign;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("")
public interface FaceMatchingController {
    @PostMapping("")
    ResponseEntity<?> getUserByFaceMatching(String userDoc, String userVideo);
}

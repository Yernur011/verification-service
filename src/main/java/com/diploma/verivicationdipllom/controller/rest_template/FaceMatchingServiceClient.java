package com.diploma.verivicationdipllom.controller.rest_template;

import com.diploma.verivicationdipllom.domain.dto.FaceDataDTO;
import com.diploma.verivicationdipllom.domain.dto.VerifyLivenessOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class FaceMatchingServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    private final String url = "http://localhost:8000/getRepresent";


    public ResponseEntity<List<FaceDataDTO>> getRepresentation(MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file1", new org.springframework.http.HttpEntity<>(file.getResource()));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<List<FaceDataDTO>>() {});
    }


    public ResponseEntity<VerifyLivenessOutput> verifyDocWithLiveness(MultipartFile document, MultipartFile livenessCheck) {
        String url = "http://localhost:8000/verifyDocWithLiveness";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", document.getResource());
        body.add("video", livenessCheck.getResource());


        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, VerifyLivenessOutput.class);
    }




    public ResponseEntity<Boolean> verifyTwoPics(MultipartFile file1, MultipartFile file2) throws IOException {
        String url = "http://localhost:8000/verifyTwoPics";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file1", new ByteArrayResource(file1.getBytes()) {
            @Override
            public String getFilename() {
                return file1.getOriginalFilename();
            }
        });
        body.add("file2", new ByteArrayResource(file2.getBytes()) {
            @Override
            public String getFilename() {
                return file2.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, Boolean.class);
    }
}

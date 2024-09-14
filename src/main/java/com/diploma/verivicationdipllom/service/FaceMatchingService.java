package com.diploma.verivicationdipllom.service;

import com.diploma.verivicationdipllom.controller.rest_template.FaceMatchingServiceClient;
import com.diploma.verivicationdipllom.domain.dto.FaceDataDTO;
import com.diploma.verivicationdipllom.domain.dto.SimilarityResult;
import com.diploma.verivicationdipllom.domain.dto.UserDocumentDto;
import com.diploma.verivicationdipllom.domain.dto.VerifyLivenessOutput;
import com.diploma.verivicationdipllom.domain.entity.UserRepresent;
import com.diploma.verivicationdipllom.domain.enums.BucketType;
import com.diploma.verivicationdipllom.domain.repository.UserRepresentRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FaceMatchingService {
    private final MinioService minioService;
    private final UserRepresentRepository userRepresentRepository;
    private final FaceMatchingServiceClient faceMatchingServiceClient;
    private final MultipartFileConverterService multipartFileConverterService;


    public void faceMatching(MultipartFile userDocument, MultipartFile userLiveness) {

        // Получаем представление лица
        ResponseEntity<List<FaceDataDTO>> representResponse = faceMatchingServiceClient.getRepresentation(userDocument);
        validateResponse(representResponse);

        // Извлекаем данные представления лица
        FaceDataDTO faceData = representResponse.getBody().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No face data found"));

        // Проверяем схожесть в базе данных
        checkFromDataBaseIsUserSimilar(userDocument, userLiveness, faceData.getEmbedding());

        // Проверяем живость
        ResponseEntity<VerifyLivenessOutput> livenessResponse
                = faceMatchingServiceClient.verifyDocWithLiveness(userDocument, userLiveness); //todo написать
        validateResponse(livenessResponse);

        // Если оценка выше порогового значения, загружаем документ и сохраняем информацию о пользователе
        if (livenessResponse.getBody().getScore() > 70) {
            UUID userId = UUID.randomUUID();
            String dateFolder = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String docPath = dateFolder + BucketType.DOCUMENT_PATH.getValue();

            UserRepresent userRepresent = UserRepresent.builder()
                    .id(userId)
                    .date(docPath)
                    .representation(faceData.getEmbedding())
                    .build();

            UserDocumentDto userDocumentDto = UserDocumentDto.builder()
                    .documentId(userId)
                    .userDocument(userDocument)
                    .userLiveness(userLiveness)
                    .bucketType(BucketType.VERIFICATION_USER)
                    .build();

            minioService.uploadUserDocAndLiveness(userDocumentDto);
            saveUserRepresent(userRepresent);
        }
    }
    private void checkFromDataBaseIsUserSimilar(MultipartFile userDocument, MultipartFile userLiveness, double[] represent) {
        SimilarityResult representResult = searchFromDataBaseIsUserSimilar(represent);

        if (representResult.isSimilar()) {
            String imagePath = representResult.getDate() + representResult.getUserId().toString();
            MultipartFile image = getImageFromBucket(BucketType.VERIFICATION_USER.getValue(), imagePath);

            if (verifyUserDocumentWithImage(userDocument, image)) {
                throw new RuntimeException("User exists in database"); //todo: заменить на конкретное исключение
            }
        }
    }

    private MultipartFile getImageFromBucket(String bucketName, String imagePath) {
        try {
            return minioService.getImage(bucketName, imagePath);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving image from MinIO", e);
        }
    }

    @SneakyThrows
    private boolean verifyUserDocumentWithImage(MultipartFile userDocument, MultipartFile image) {
        ResponseEntity<Boolean> response = faceMatchingServiceClient.verifyTwoPics(userDocument, image);
        if (response.getStatusCode().is2xxSuccessful()) {
            return Boolean.TRUE.equals(response.getBody());
        } else {
            throw new RuntimeException("Failed to verify images: " + response.getStatusCode()); //todo: заменить на конкретное исключение
        }
    }

    public SimilarityResult searchFromDataBaseIsUserSimilar(double[] represent) {
        return StreamSupport.stream(userRepresentRepository.findAll().spliterator(), true) // true для параллельного потока
                .filter(userRepresent -> cosineSimilarity(userRepresent.getRepresentation(), represent) > 0.6)
                .findFirst()
                .map(userRepresent -> new SimilarityResult(userRepresent.getId(), true, userRepresent.getDate()))
                .orElse(new SimilarityResult(null, false, null));
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public UserRepresent saveUserRepresent(UserRepresent userRepresent) {
        return userRepresentRepository.save(userRepresent);
    }

    @SneakyThrows
    public ResponseEntity<VerifyLivenessOutput> verifyDocWithLiveness(MultipartFile userDocument, MultipartFile userLiveness){
        List<MultipartFile> multipartFiles = multipartFileConverterService.extractFrames(userLiveness);
        List<Boolean> booleans = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            booleans.add(faceMatchingServiceClient.verifyTwoPics(userDocument, multipartFile).getBody());
        }
        List<Boolean> list = booleans.stream().filter(bool -> bool == true).toList();
        int size1 = list.size();
        int size = booleans.size();
        return ResponseEntity.ok(new VerifyLivenessOutput((size1 / size) * 100, 0));
    }

    private static double cosineSimilarity(double[] vectorA, double[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vectors must be of the same length");
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (normA * normB);
    }

    private <T> void validateResponse(ResponseEntity<T> responseEntity) {
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to get successful response from service: " + responseEntity.getStatusCode());
        }
    }


//    public void faceMatching(MultipartFile userDocument, MultipartFile userLiveness) {
//        ResponseEntity<List<FaceDataDTO>> represent = faceMatchingServiceClient.getRepresentation(userDocument);
//        if (!represent.getStatusCode().is2xxSuccessful()) {
//            throw new RuntimeException(); //todo exception
//        }
//        checkFromDataBaseIsUserSimilar(userDocument, userLiveness, Objects.requireNonNull(represent.getBody()).stream().findFirst().get().getEmbedding());
//        ResponseEntity<VerifyLivenessOutput> responseEntity = faceMatchingServiceClient.verifyDocWithLiveness(userDocument, userLiveness);
//        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
//            throw new RuntimeException(); //todo exception
//        }
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
//        String dateFolder = sdf.format(new Date());
//        UUID uuidForUsers = UUID.randomUUID();
//        String doc = dateFolder + BucketType.DOCUMENT_PATH.getValue();
//        UserRepresent userRepresent = UserRepresent.builder().id(uuidForUsers).date(doc).representation(represent.getBody().stream().findFirst().get().getEmbedding()).build();
//
//        if (Objects.requireNonNull(responseEntity.getBody()).getScore() > 70) {
//            UserDocumentDto build = UserDocumentDto.builder().documentId(uuidForUsers).userDocument(userDocument).userLiveness(userLiveness).bucketType(BucketType.VERIFICATION_USER).build();
//            minioService.uploadUserDocAndLiveness(build);
//            saveUserRepresent(userRepresent);
//        }

//    }
//    @SneakyThrows
//    private void checkFromDataBaseIsUserSimilar(MultipartFile userDocument, MultipartFile userLiveness, double[] represent) {
//        SimilarityResult representResult = searchFromDataBaseIsUserSimilar(represent);
//        if (representResult.isSimilar()) {
//            MultipartFile image = minioService.getImage(BucketType.VERIFICATION_USER.getValue(), representResult.getDate() + representResult.getUserId().toString());
//            ResponseEntity<Boolean> booleanResponseEntity = faceMatchingServiceClient.verifyTwoPics(userDocument, image);
//            if (booleanResponseEntity.getStatusCode().is2xxSuccessful()) {
//                if (Boolean.TRUE.equals(booleanResponseEntity.getBody())) {
//                    throw new RuntimeException("user exist in database");//todo
//                }
//            } else {
//                throw new RuntimeException(); //todo
//            }
//        }
//    }

//
    //    @Transactional
//    public boolean searchFromDataBaseIsUserSimilar(double[] represent) {
//        return StreamSupport.stream(userRepresentRepository.findAll().spliterator(), true)
//                .anyMatch(users -> (1 - cosineSimilarity(users.getRepresentation(), represent)) > 0.6);
//    }
//    @Transactional
//    public SimilarityResult searchFromDataBaseIsUserSimilar(double[] represent) {
//        return StreamSupport.stream(userRepresentRepository.findAll().spliterator(), true)
//                .map(user -> new SimilarityResult(user.getId(), (1 - cosineSimilarity(user.getRepresentation(), represent)) > 0.6))
//                .filter(SimilarityResult::isSimilar)
//                .findFirst()
//                .orElse(new SimilarityResult(null, false));

//    }

    //    public SimilarityResult searchFromDataBaseIsUserSimilar(double[] represent) {
//        Iterable<UserRepresent> all = userRepresentRepository.findAll();
//        for (UserRepresent userRepresent : all) {
//            if (cosineSimilarity(userRepresent.getRepresentation(), represent) > 0.6){
//                return new SimilarityResult(userRepresent.getId(), true, userRepresent.getDate());
//            }
//        }
//        return new SimilarityResult(null, false, null);

//    }
}

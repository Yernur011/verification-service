package com.diploma.verivicationdipllom.service;

import com.diploma.verivicationdipllom.domain.dto.UserDocumentDto;
import com.diploma.verivicationdipllom.domain.enums.BucketType;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class MinioService {

    private final MinioClient minioClient;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public void uploadImage(String bucketName, String objectName, MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (MinioException e) {
            throw new RuntimeException("Error occurred while uploading the image: " + e.getMessage());
        }
    }
    public void uploadDocument(UserDocumentDto userDocumentDto) throws Exception {
        try (InputStream inputStream = userDocumentDto.getUserDocument().getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(userDocumentDto.getBucketType().getValue())
                            .object(userDocumentDto.getDocumentId().toString())
                            .stream(inputStream, userDocumentDto.getUserDocument().getSize(), -1)
                            .contentType(userDocumentDto.getUserDocument().getContentType())
                            .build()
            );
        } catch (MinioException e) {
            throw new RuntimeException("Error occurred while uploading the image: " + e.getMessage());
        }
    }

    public InputStream getImage(String bucketName, String objectName) throws Exception {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (MinioException e) {
            throw new RuntimeException("Error occurred while retrieving the image: " + e.getMessage());
        }
    }


    @SneakyThrows
    public void createBucketIfNotExists(String bucketName) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("Bucket created: " + bucketName);
            } else {
                System.out.println("Bucket already exists: " + bucketName);
            }
        } catch (MinioException e) {
            throw new RuntimeException("Error occurred while creating bucket: " + e.getMessage(), e);
        }
    }


    public List<byte[]> findAllFilesInUserDocumentBucket() {
        List<byte[]> files = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(BucketType.VERIFICATION_USER.getValue())
                    .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                        .bucket(BucketType.VERIFICATION_USER.getValue())
                        .object(item.objectName())
                        .build())) {

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = stream.read(buffer)) != -1) {
                        baos.write(buffer, 0, length);
                    }
                    files.add(baos.toByteArray());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Логгирование и обработка ошибок
        }

        return files;
    }

}


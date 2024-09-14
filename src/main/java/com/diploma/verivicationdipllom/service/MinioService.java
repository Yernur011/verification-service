package com.diploma.verivicationdipllom.service;

import com.diploma.verivicationdipllom.domain.dto.CustomMultipartFile;
import com.diploma.verivicationdipllom.domain.dto.SaveUserDto;
import com.diploma.verivicationdipllom.domain.dto.UserDocumentDto;
import com.diploma.verivicationdipllom.domain.enums.BucketType;
import io.minio.*;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MinioService {

    private final MinioClient minioClient;

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

    @SneakyThrows
    public void uploadUserDocAndLiveness(UserDocumentDto userDocumentDto) {
        // Форматирование текущей даты в виде папки
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String dateFolder = sdf.format(new Date());

        // Генерация полного пути для объекта (папка с датой + ID документа)
        String doc = dateFolder + BucketType.DOCUMENT_PATH.getValue() + userDocumentDto.getDocumentId();
        String liveness = dateFolder + BucketType.LIVENESS_PATH.getValue() + userDocumentDto.getDocumentId();


        // save user document
        SaveUserDto saveUserDto = SaveUserDto
                .builder()
                .documentId(userDocumentDto.getDocumentId())
                .bucketType(BucketType.VERIFICATION_USER)
                .bucketPath(doc)
                .userDocument(userDocumentDto.getUserDocument())
                .build();
        saveDocument(saveUserDto);

        // save liveness
        saveUserDto = SaveUserDto
                .builder()
                .documentId(userDocumentDto.getDocumentId())
                .bucketType(BucketType.VERIFICATION_USER)
                .bucketPath(liveness)
                .userDocument(userDocumentDto.getUserLiveness())
                .build();
        saveDocument(saveUserDto);
    }

    private void saveDocument(SaveUserDto saveUserDto) throws Exception {
        try (InputStream inputStream = saveUserDto.getUserDocument().getInputStream()) {
            // Загрузка файла в MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(saveUserDto.getBucketType().getValue())
                            .object(saveUserDto.getBucketPath())  // Используем путь с папкой и именем файла
                            .stream(inputStream, saveUserDto.getUserDocument().getSize(), -1)
                            .contentType(saveUserDto.getUserDocument().getContentType())
                            .build()
            );

        } catch (MinioException e) {
            throw new RuntimeException("Error occurred while uploading the image: " + e.getMessage());
        }
    }

    public MultipartFile getImage(String bucketName, String objectName) throws Exception {
        try {
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            // Convert InputStream to byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }

            byte[] fileBytes = byteArrayOutputStream.toByteArray();

            // Create MultipartFile
            return new CustomMultipartFile(
                    objectName,  // Name
                    objectName,  // Original Filename
                    "image/jpeg",  // Content Type
                    fileBytes
            );

        } catch (MinioException | IOException e) {
            throw new RuntimeException("Error occurred while retrieving the image: " + e.getMessage(), e);
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
}


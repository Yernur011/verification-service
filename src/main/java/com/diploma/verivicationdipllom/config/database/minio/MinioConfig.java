package com.diploma.verivicationdipllom.config.database.minio;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint("http://localhost:9001") // URL вашего MinIO сервера, порт 9002, перенаправленный на 9000 в контейнере
                .credentials("admin", "yourpassword") // Имя пользователя и пароль из Docker Compose файла
                .build();
    }
}

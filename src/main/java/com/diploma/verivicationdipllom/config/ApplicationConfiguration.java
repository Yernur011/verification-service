package com.diploma.verivicationdipllom.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // Максимальный размер файла
        factory.setMaxFileSize(DataSize.ofGigabytes(1L));
        // Максимальный размер всего запроса
        factory.setMaxRequestSize(DataSize.ofGigabytes(1L));
        return factory.createMultipartConfig();
    }
}

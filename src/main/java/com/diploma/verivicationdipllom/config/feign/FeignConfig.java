package com.diploma.verivicationdipllom.config.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.ErrorDecoder;
import feign.okhttp.OkHttpClient;
import org.springframework.context.annotation.Bean;

public interface FeignConfig extends RequestInterceptor {
    @Bean
    default OkHttpClient client() {
        return new OkHttpClient();
    }
    @Bean
    default ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    @Override
    default void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("Content-Type", "application/json");
    }

}


package com.diploma.verivicationdipllom.config.feign;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.apache.coyote.BadRequestException;
import org.springframework.data.crossstore.ChangeSetPersister;

public class CustomErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 400 -> new BadRequestException();
            case 404 -> new ChangeSetPersister.NotFoundException();
            default -> new Exception("Generic error " + response.status());
        };
    }
}
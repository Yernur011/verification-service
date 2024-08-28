package com.diploma.verivicationdipllom.domain.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPhotoDto {
    UUID imageId;
    String bucketName;
    MultipartFile userDocument;
}

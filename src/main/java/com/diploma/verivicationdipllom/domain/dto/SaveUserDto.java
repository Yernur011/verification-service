package com.diploma.verivicationdipllom.domain.dto;

import com.diploma.verivicationdipllom.domain.enums.BucketType;
import io.minio.messages.Bucket;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveUserDto {
    UUID documentId;
    MultipartFile userDocument;
    BucketType bucketType;
    String bucketPath;
}

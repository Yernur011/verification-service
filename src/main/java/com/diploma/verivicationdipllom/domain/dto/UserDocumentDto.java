package com.diploma.verivicationdipllom.domain.dto;

import com.diploma.verivicationdipllom.domain.enums.BucketType;
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
public class UserDocumentDto {
    UUID documentId;
    MultipartFile userDocument;
    MultipartFile userLiveness;
    BucketType bucketType;
}

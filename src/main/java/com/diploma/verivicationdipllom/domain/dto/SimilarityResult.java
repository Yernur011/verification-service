package com.diploma.verivicationdipllom.domain.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SimilarityResult {
    private UUID userId;
    private boolean isSimilar;
    private String date;
}

package com.diploma.verivicationdipllom.domain.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class VerifyLivenessOutput {
    Integer score;
    Integer scoredTimeInSeconds;
}

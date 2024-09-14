package com.diploma.verivicationdipllom.domain.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

@Document(indexName = "user_represent")
public class UserRepresent {
    UUID id;
    double[] representation;
    String date;
}

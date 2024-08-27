package com.diploma.verivicationdipllom.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEntityDto {
    UUID id;
    String userDocument;
    String userMediaLive;
}
